#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

// ------------------------ Boolean Strings ------------------------

static const char* const TRUE = "True";
static const char* const FALSE = "False";

// ------------------------ States ------------------------

static const char* const ATTACK = "attack";
static const char* const FLIP_VISIBILITY = "flip_visibility";
static const char* const HEAL = "heal";
static const char* const SUPER = "super";

/** Maximum allowed power for the player */
static const uint16_t MAX_POWER = 1000;

// ------------------------ Macro-functions ------------------------

// #define __DEBUG

/**
 * Removes all written data in output file, prints "Invalid inputs" and
 * goes to FREE_AND_EXIT block (to free memory and close files)
 *
 * @param FOUT output file
 */

#define INVALID_INPUT_PANIC(FOUT)   \
    fclose(FOUT);                   \
    FOUT = fopen("output.txt", "w");\
    fputs("Invalid inputs\n", FOUT);\
    goto FREE_AND_EXIT

/**
 * Reads string from FIN and parses number according to given FORMAT.
 * Read number is written to NUMBER. If parsing is not successful,
 * panics with INVALID_INPUT_PANIC
 *
 * @param FIN input file
 * @param FORMAT number format according to printf
 * @param NUMBER variable to write read number
 * @param FOUT output file
 */

#define READ_NUMBER_OR_PANIC(FIN, FORMAT, NUMBER, FOUT)                                                     \
    char __READ_NUMBER_OR_PANIC_BUF_##NUMBER[100];                                                          \
    if (fgets(__READ_NUMBER_OR_PANIC_BUF_##NUMBER, 100, FIN) == NULL) { INVALID_INPUT_PANIC(FOUT); }        \
    if (sscanf(__READ_NUMBER_OR_PANIC_BUF_##NUMBER, FORMAT, &NUMBER) != 1) { INVALID_INPUT_PANIC(FOUT); }   \
    if (strcnt(__READ_NUMBER_OR_PANIC_BUF_##NUMBER, ' ') != 0) { INVALID_INPUT_PANIC(FOUT); }

/**
 * Trims string's end
 * @param STR string to trim
 */

#define TRIM_STRING_END(STR) STR[strcspn(STR, "\n")] = '\0'

/**
 * Reads string and trims with TRIM_STRING_END
 * @param STR buffer for string
 * @param FIN input file
 * @param FOUT output file
 */

#define READ_AND_TRIM_OR_PANIC(STR, FIN, FOUT)                      \
    if (fgets(STR, 100, FIN) == NULL) { INVALID_INPUT_PANIC(FOUT); }\
    TRIM_STRING_END(STR)

/**
 * Reads string with READ_AND_TRIM_OR_PANIC,
 * Checks if it's a name with is_name_correct().
 * If there are input errors, panics with INVALID_INPUT_PANIC
 */

#define READ_AND_CHECK_NAME_OR_PANIC(STR, FIN, FOUT) \
    READ_AND_TRIM_OR_PANIC(STR, FIN, FOUT);          \
    if (!is_name_correct(STR)) { INVALID_INPUT_PANIC(FOUT); }

/** Does exactly what you think it does */
#define MAX_NUMBER(ARG1, ARG2) (ARG1) > (ARG2) ? (ARG1) : (ARG2)

/** Does exactly what you think it does */
#define MIN_NUMBER(ARG1, ARG2) (ARG1) < (ARG2) ? (ARG1) : (ARG2)

// ------------------------ Utility function ------------------------

/**
 * Count characters in the string according to the pattern
 * @param str string itself
 * @param pattern character to count
 * @returns number of characters in the string
 */

static size_t strcnt(const char* const str, const char pattern) {
    size_t cnt = 0;

    for (const char* c = str; *c != '\0'; ++c)
        if (*c == pattern)
            ++cnt;

    return cnt;
}

/**
 * Checks if a given name is valid
 * @param name name to check
 * @returns 1 if it is ok, 0 otherwise
 */

static int is_name_correct(char* const name) {
    const size_t len = strlen(name);

    if (len < 2 || len > 20)
        return 0;

    if (!isalpha(*name)) return 0;
    if (islower(*name)) return 0;

    for (char* c = name + 1; c != name + len; ++c)
        if (!isalpha(*c))
            return 0;

    return 1;
}

// ------------------------ Player ------------------------

/**
 * Player structure
 * @field name name of player (valid according to is_name_correct)
 * @field power power of player (number in [0..1000]; if 0, than player is frozen, otherwise not frozen)
 * @field team_number team number (number in [0 until number_of_teams])
 * @field is_visible is player visible (0 or 1)
 */

typedef struct {
    char* name;
    uint16_t power: 10;
    uint8_t team_number: 4;
    uint8_t is_visible: 1;
} player;

// Creates new player without any checks
// @param name: name of the player (must be valid)
// @param team_number: number of the team (number in [0 until number of team])
// @param power: power of the player (number in [0..1000])
// @param is_visible: is player visible [0 or 1]
// @returns new player's entity in heap

static player* __player_new_unchecked(
        const char* const name,
        const uint16_t team_number,
        const uint16_t power,
        const uint8_t is_visible
) {
    player* pl = malloc(sizeof(player));
    pl->name = strdup(name);
    pl->power = power;
    pl->team_number = team_number;
    pl->is_visible = is_visible;
    return pl;
}

// Creates new player entity with filed limitation checks
// @param name: name of the player (checked with is_name_correct)
// @param team_number: number in [0 until number_of_teams]
// @param power: number in [0..1000]
// @param visibility: visibility as string [True/False]
// @param number_of_teams: number of teams itself

static player* __player_new(
        char* const name,
        const uint16_t team_number,
        const uint16_t power,
        const char* const visibility,
        const size_t number_of_teams
) {
    if (!is_name_correct(name))
        return NULL;

    if (team_number >= number_of_teams)
        return NULL;

    if (power > MAX_POWER)
        return NULL;

    uint8_t is_visible;

    if (strcmp(visibility, TRUE) == 0)
        is_visible = 1;
    else if (strcmp(visibility, FALSE) == 0)
        is_visible = 0;
    else
        return NULL;

    return __player_new_unchecked(name, team_number, power, is_visible);
}

// Sets player's power to zero
// @param player: player to freeze

static void __player_freeze(player* const player) {
    player->power = 0;
}

// Increases player's power according
// to MAX_POWER limitation (MIN(power, MAX_POWER))
//
// @param player: player itself
// @param power: new power to set

static void __player_increase_power(player* const player, const uint16_t power) {
    const uint16_t sum_power = player->power + power;
    player->power = MIN_NUMBER(sum_power, MAX_POWER);
}

// Frees name's buffer of player
// @param player: player itself

static void __player_free(player* const player) {
    free(player->name);
}

// Debugs player's data
// @param player: player to debug

static void __player_debug(const player player) {
#ifdef __DEBUG
    printf("PLAYER: %s %u %u %u %u\n", player.name, player.team_number, player.power, player.is_visible, player.is_frozen);
#endif
}

/** Static part of player's structure with all methods above */

static const struct {
    /**
     * Creates new player entity with filed limitation checks
     * @param name name of the player (checked with is_name_correct)
     * @param team_number number in [0 until number_of_teams]
     * @param power number in [0..1000]
     * @param visibility visibility as string [True/False]
     * @param number_of_teams number of teams itself
     */

    player* (*new_unchecked)(const char* const name, const uint16_t team_number, const uint16_t power, const uint8_t is_visible);

    /**
     * Creates new player entity with filed limitation checks
     * @param name name of the player (checked with is_name_correct)
     * @param team_number number in [0 until number_of_teams]
     * @param power number in [0..1000]
     * @param visibility visibility as string [True/False]
     * @param number_of_teams number of teams itself
     */

    player* (*new)(char* const name, const uint16_t team_number, const uint16_t power, const char* const visibility, const size_t number_of_teams);

    /**
     * Sets player's power to zero
     * @param player player to freeze
     */

    void (*freeze)(player* const self);

    /**
     * Increases player's power according
     * to MAX_POWER limitation (MIN(power, MAX_POWER))
     * @param player player itself
     * @param power new power to set
     */

    void (*increase_power)(player* const self, const uint16_t power);

    /**
     * Frees name's buffer of player
     * @param player player itself
     */

    void (*free_player_data)(player* const self);

    /**
     * Debugs player's data
     * @param player player to debug
     */

    void (*debug)(const player self);
} static_player = {
        .new_unchecked = __player_new_unchecked,
        .new = __player_new,
        .freeze = __player_freeze,
        .increase_power = __player_increase_power,
        .free_player_data = __player_free,
        .debug = __player_debug
};

// ------------------------ Teams ------------------------

/**
 * Teams structure with its number and accumulated power
 * @field number number of team (number in [0 until number_of_teams])
 * @field power accumulated power of the whole members of the team
 */

typedef struct {
    uint8_t number;
    int32_t power;
} team;

/**
 * Comparator for teams (by power in descending order)
 * @param arg1 first team as void pointer
 * @param arg2 second team as void pointer
 * @return more than 0 if t1.power > t2.power;
 *         less than 0 if t1.power < t2.power;
 *                   0 if t1.power = t2.power
 */

static int team_cmp(const void* const arg1, const void* const arg2) {
    const team g1 = *(const team* const) arg1;
    const team g2 = *(const team* const) arg2;
    return g2.power - g1.power;
}

// ------------------------ Player Binary Tree ------------------------

/**
 * Represents node of the tree
 * @field player player itself or null
 */

typedef struct player_tree_node {
    player* player;
    struct player_tree_node* __left; // smaller node (compare by name lexicographically)
    struct player_tree_node* __right; // bigger node (compare by name lexicographically)
} player_tree_node;

// Creates new tree node in heap
// @param player: player that will be stored in the node
// @return created node with both left and right nodes as null
// @warning Function clones player's reference, not data.
// player's lifetime should be equal to node's lifetime

player_tree_node* __player_tree_node_new(player* const player) {
    player_tree_node* const node = malloc(sizeof(player_tree_node));
    node->player = player;
    node->__left = NULL;
    node->__right = NULL;
    return node;
}

/**
 * Represents the whole player's tree.
 * This implementation is a basic (not balanced) binary tree,
 * any operations will cost O(log n) in worst case
 */

typedef struct {
    player_tree_node* __root;   // root node
    size_t __size;              // number of players in the tree
} player_tree;

// Creates empty tree on stack
// @return created tree without any nodes

player_tree __player_tree_new() {
    const player_tree tree = { .__size = 0, .__root = NULL };
    return tree;
}

// Creates empty tree on heap
// @return created tree's pointer without any nodes

player_tree* __player_tree_allocate() {
    player_tree* const tree = malloc(sizeof(player_tree));
    tree->__size = 0;
    tree->__root = NULL;
    return tree;
}

// Gets tree's size
// @param tree: tree itself
// @return size of tree

size_t __player_tree_get_size(const player_tree tree) {
    return tree.__size;
}

// Inserts player into the node's tree
// @param tree_node: root node of insertable segment
// @param player: player to insert
// @return updated node

player_tree_node* __player_tree_node_insert(player_tree_node* tree_node, player* const player) {
    if (tree_node == NULL)
        return __player_tree_node_new(player);

    const int cmp = strcmp(player->name, tree_node->player->name);

    if (cmp < 0)
        tree_node->__left = __player_tree_node_insert(tree_node->__left, player);
    else if (cmp > 0)
        tree_node->__right = __player_tree_node_insert(tree_node->__right, player);
    else
        return NULL;

    return tree_node;
}

// Inserts player into the node's tree
// @param tree_node: root node of insertable segment
// @param player: player to insert
// @return 1 if player is successfully added, otherwise 0

int __player_tree_insert(player_tree* const tree, player* const player) {
    ++tree->__size;
    player_tree_node* const new = __player_tree_node_insert(tree->__root, player);

    if (new != NULL) {
        tree->__root = new;
        return 1;
    } else {
        return 0;
    }
}

// Searches for the player by his name
// Asymptotic complexity is O(log n) in worst case
// @param node: root of the searchable tree's fragment
// @param name: searchable player's name
// @return pointer on found player or null

player* __player_tree_node_find(player_tree_node* const node, const char* const name) {
    if (node == NULL || node->player == NULL)
        return NULL;

    const int cmp = strcmp(name, node->player->name);

    return cmp == 0 ?
           node->player : cmp < 0 ?
                __player_tree_node_find(node->__left, name) :
                __player_tree_node_find(node->__right, name);
}

// Searches for the player by his name
// Asymptotic complexity is O(log n) in worst case
// @param tree: tree itself
// @param name: searchable player's name
// @return pointer on found player or null

player* __player_tree_find(player_tree* const tree, const char* const name) {
    return __player_tree_node_find(tree->__root, name);
}

// Gets node with the smallest player's name
// @param tree_node: root of the given tree's segment
// @return smallest node or null if there are no nodes

player_tree_node* __player_tree_node_min(player_tree_node* const tree_node) {
    player_tree_node* current = tree_node;

    while (current != NULL && current->__left != NULL)
        current = current->__left;

    return current;
}

// Gets node with the smallest player's name
// @param tree: tree itself
// @return smallest node or null if there are no nodes

player_tree_node* __player_tree_min(player_tree* const tree) {
    return __player_tree_node_min(tree->__root);
}

// Gets node with the biggest player's name
// @param tree_node: root of the given tree's segment
// @return biggest node or null if there are no nodes

player_tree_node* __player_tree_node_max(player_tree_node* const tree_node) {
    player_tree_node* current = tree_node;

    while (current != NULL && current->__right != NULL)
        current = current->__right;

    return current;
}

// Gets node with the biggest player's name
// @param tree: tree itself
// @return biggest node or null if there are no nodes

player_tree_node* __player_tree_max(player_tree* const tree) {
    return __player_tree_node_max(tree->__root);
}

// Frees memory for this particular node only
// @params tree_node: clearable node
// @warning The node must be a leaf, so there can't be any child nodes.
// If you want to clear the whole fragment, use __player_tree_node_free()

void __player_tree_node_free_this(player_tree_node* const tree_node) {
    static_player.free_player_data(tree_node->player);
    free(tree_node->player);
    free(tree_node);
}

// Frees memory for the whole tree's fragment
// @param tree_node: root of the clearable fragment

void __player_tree_node_free(player_tree_node* const tree_node) {
    if (tree_node == NULL)
        return;

    if (tree_node->player != NULL) {
        static_player.free_player_data(tree_node->player);
        free(tree_node->player);
    }

    __player_tree_node_free(tree_node->__left);
    __player_tree_node_free(tree_node->__right);
    free(tree_node);
}

// Frees memory for the whole tree
// @param tree: tree itself

void __player_tree_free(player_tree* const tree) {
    tree->__size = 0;
    __player_tree_node_free(tree->__root);
    free(tree);
}

// Removes (clears memory) the node by given player's name.
// Also replaces child nodes of the removed node
// @param node: root of tree's fragment
// @param name: name of searched player
// @return updated node

player_tree_node* __player_tree_node_remove(player_tree_node* const node, const char* const name) {
    if (node == NULL || node->player == NULL)
        return NULL;

    const int cmp = strcmp(name, node->player->name);

    if (cmp < 0) // goes to left and updates left node
        node->__left = __player_tree_node_remove(node->__left, name);
    else if (cmp > 0) // goes to right and updates right node
        node->__right = __player_tree_node_remove(node->__right, name);
    else {
        // this node is a searched node

        if (node->__left == NULL) {
            // if the left node is empty, this node
            // will be replaced with the right child
            player_tree_node* const tmp = node->__right;
            __player_tree_node_free_this(node);
            return tmp;
        }

        if (node->__right == NULL) {
            // if the right node is empty, this node
            // will be replaced with the left child
            player_tree_node* const tmp = node->__left;
            __player_tree_node_free_this(node);
            return tmp;
        }

        // If both children nodes exists, this player will be replaced
        // with the next player in the lexicographical order
        const player_tree_node* const tmp = __player_tree_node_min(node->__right);
        node->player = tmp->player;
        node->__right = __player_tree_node_remove(node->__right, tmp->player->name);
    }

    return node;
}

// Removes (clears memory) the node by given player's name.
// Also replaces child nodes of the removed node
// @param tree: tree itself
// @param name: name of searched player

void __player_tree_remove(player_tree* const tree, const char* const name) {
    --tree->__size;
    tree->__root = __player_tree_node_remove(tree->__root, name);
}

// Collects power from all players
// in the particular tree's fragment to the teams
// @param tree_node: root of tree's fragment
// @param teams: teams of players

void __player_tree_node_collect_power(player_tree_node* const tree_node, team* const teams) {
    if (tree_node == NULL || tree_node->player == NULL)
        return;

    teams[tree_node->player->team_number].power += tree_node->player->power;
    __player_tree_node_collect_power(tree_node->__left, teams);
    __player_tree_node_collect_power(tree_node->__right, teams);
}

// Collects power from all players in the tree to the teams
// @param tree: tree itself
// @param teams: teams of players

void __player_tree_collect_power(player_tree* const tree, team* const teams) {
    __player_tree_node_collect_power(tree->__root, teams);
}

// Debugs the particular node's data
// (player's data and level of the node in the fragment)
// @param node: root of tree's fragment
// @param level: level of the current node from the root of parent fragment

void __player_tree_node_debug(const player_tree_node* const node, int level) {
#ifdef __DEBUG
    if (node == NULL)
        return;

    printf("NODE ELEM: %s LEVEL: %d\n", node->player->name, level);
    __player_tree_node_debug(node->__left, level + 1);
    __player_tree_node_debug(node->__right, level + 1);
#endif
}

// Debugs the whole tree's data (player's data and level of nodes)
// @param self: tree itself
// @param level: level of the current node from the root of the tree

void __player_tree_debug(const player_tree* const tree) {
#ifdef __DEBUG
    __player_tree_node_debug(tree->__root, 0);
#endif
}

static const struct {
    /**
     * Creates empty tree on stack
     * @return created tree without any nodes
     */

    player_tree (*new)();

    /**
     * Creates empty tree on heap
     * @return created tree's pointer without any nodes
     */

    player_tree* (*allocate)();

    /**
     * Gets tree's size
     * @param self tree itself
     * @return size of tree
     */

    size_t (*get_size)(const player_tree self);

    /**
     * Inserts player into the node's tree
     * @param self tree itself
     * @param player player to insert
     * @return 1 if player is successfully added, otherwise 0
     */

    int (*insert)(player_tree* const self, player* const player);

    /**
     * Searches for the player by his name
     * Asymptotic complexity is O(log n) in worst case
     *
     * @param self tree itself
     * @param name searchable player's name
     * @return pointer on found player or null
     */

    player* (*find)(player_tree* const self, const char* const name);

    /**
     * Gets node with the smallest player's name
     * @param self tree itself
     * @return smallest node or null if there are no nodes
     */

    player_tree_node* (*min)(player_tree* const self);

    /**
     * Gets node with the biggest player's name
     * @param self tree itself
     * @return biggest node or null if there are no nodes
     */

    player_tree_node* (*max)(player_tree* const self);

    /**
     * Frees memory for the whole tree
     * @param tree tree itself
     */

    void (*free)(player_tree* const self);

    /**
     * Removes (clears memory) the node by given player's name.
     * Replaces child nodes of the removed node, so all child elements will stay valid
     *
     * @param self tree itself
     * @param name name of searched player
     */

    void (*remove)(player_tree* const self, const char* const name);

    /**
     * Collects power from all players in the self to the teams
     * @param self self itself
     * @param teams teams of players
     */

    void (*collect_power)(player_tree* const self, team* const teams);

    /**
     * Debugs the whole tree's data (player's data and level of nodes)
     * @param self tree itself
     * @param level level of the current node from the root of the tree
     */

    void (*debug)(const player_tree* const self);
} static_player_tree = {
        .new = __player_tree_new,
        .allocate = __player_tree_allocate,
        .get_size = __player_tree_get_size,
        .insert = __player_tree_insert,
        .find = __player_tree_find,
        .min = __player_tree_min,
        .max = __player_tree_max,
        .free = __player_tree_free,
        .remove = __player_tree_remove,
        .collect_power = __player_tree_collect_power,
        .debug = __player_tree_debug
};

// ------------------------ Utilities for the states ------------------------

/** Result of actions */

typedef enum {
    INPUT_ERROR,
    PLAYER_IS_INVISIBLE,
    PLAYER_IS_FROZEN,
    WRONG_TEAM,
    HEAL_SELF,
    SUPER_SELF,
    OK
} action_status;

/**
 * Reads two players and checks if they are exists.
 * If there are any input errors, will return INPUT_ERROR status
 *
 * @param PLAYER1_VARIABLE variable in which 1-st player will be stored
 * @param PLAYER2_VARIABLE variable in which 2-nd player will be stored
 * @param PLAYERS player's tree
 * @param FIN input file
 */

#define GET_TWO_PLAYERS(PLAYER1_VARIABLE, PLAYER2_VARIABLE, PLAYERS, FIN)                                                                               \
    char __GET_TWO_PLAYERS_ACTION[100], __GET_TWO_PLAYERS_PLAYER1_NAME[100], __GET_TWO_PLAYERS_PLAYER2_NAME[100];                                       \
    if (sscanf(FIN, "%s %s %s", __GET_TWO_PLAYERS_ACTION, __GET_TWO_PLAYERS_PLAYER1_NAME, __GET_TWO_PLAYERS_PLAYER2_NAME) != 3) return INPUT_ERROR;     \
    if (strcnt(FIN, ' ') != 2) return INPUT_ERROR;                                                                                                \
    player* const PLAYER1_VARIABLE = static_player_tree.find(PLAYERS, __GET_TWO_PLAYERS_PLAYER1_NAME);                                                  \
    if (PLAYER1_VARIABLE == NULL) return INPUT_ERROR;                                                                                                   \
    player* const PLAYER2_VARIABLE = static_player_tree.find(PLAYERS, __GET_TWO_PLAYERS_PLAYER2_NAME);                                                  \
    if (PLAYER2_VARIABLE == NULL) return INPUT_ERROR

/**
 * Does everything that GET_TWO_PLAYERS does
 * with additional checks of the 1-st player (visibility and frozen state)
 *
 * @param PLAYER1_VARIABLE variable in which 1-st player will be stored
 * @param PLAYER2_VARIABLE variable in which 2-nd player will be stored
 * @param PLAYERS player's tree
 * @param FIN input file
 */

#define GET_TWO_PLAYERS_AND_CHECK_FIRST(PLAYER1_VARIABLE, PLAYER2_VARIABLE, PLAYERS, FIN) \
    GET_TWO_PLAYERS(PLAYER1_VARIABLE, PLAYER2_VARIABLE, PLAYERS, FIN);                    \
    if (!PLAYER1_VARIABLE->is_visible) return PLAYER_IS_INVISIBLE;                        \
    if (!PLAYER1_VARIABLE->power) return PLAYER_IS_FROZEN

// ------------------------ States and State Machine ------------------------

/**
 * Represents attack state according to the task's condition
 * Also checks all input error and player's statuses and reports warnings or errors
 *
 * @param players tree of players
 * @param input read action string that will be parsed
 * @return OK if everything is ok, or particular
 * error status according to the error
 */

static action_status __attack_state_action(
        player_tree* const players,
        char* const input
) {
    GET_TWO_PLAYERS_AND_CHECK_FIRST(player1, player2, players, input);

#ifdef __DEBUG
    static_player.debug(*player1);
    static_player.debug(*player2);
    putchar('\n');
#endif

    if (!player2->is_visible) {
        static_player.freeze(player1);
        return OK;
    }

    const uint16_t power1 = player1->power;
    const uint16_t power2 = player2->power;

    if (power1 > power2) {
        static_player.increase_power(player1, power1 - power2);
        static_player.freeze(player2);
        return OK;
    }

    if (power1 < power2) {
        static_player.increase_power(player2, power2 - power1);
        static_player.freeze(player1);
        return OK;
    }

    // power1 == power2
    static_player.freeze(player1);
    static_player.freeze(player2);
    return OK;
}

/**
 * Represents flip visibility state according to the task's condition
 * Also checks all input error and player's statuses and reports warnings or errors
 *
 * @param players tree of players
 * @param input read action string that will be parsed
 * @return OK if everything is ok, or particular
 * error status according to the error
 */

static action_status __flip_visibility_state_action(
        player_tree* const players,
        char* const input
) {
    char action[100], name[100];

    if (sscanf(input, "%s %s", action, name) != 2)
        return INPUT_ERROR;

    if (strcnt(input, ' ') != 1)
        return INPUT_ERROR;

    player* const player = static_player_tree.find(players, name);

    if (player == NULL)
        return INPUT_ERROR;

    if (!player->power)
        return PLAYER_IS_FROZEN;

#ifdef __DEBUG
    static_player.debug(*player);
    putchar('\n');
#endif

    player->is_visible = !player->is_visible;
    return OK;
}

/**
 * Represents heal state according to the task's condition
 * Also checks all input error and player's statuses and reports warnings or errors
 *
 * @param players tree of players
 * @param input read action string that will be parsed
 * @return OK if everything is ok, or particular
 * error status according to the error
 */

static action_status __heal_state_action(
        player_tree* const players,
        char* const input
) {
    GET_TWO_PLAYERS_AND_CHECK_FIRST(player1, player2, players, input);

#ifdef __DEBUG
    static_player.debug(*player1);
    static_player.debug(*player2);
    putchar('\n');
#endif

    if (player1->team_number != player2->team_number)
        return WRONG_TEAM;

    if (player1 == player2)
        return HEAL_SELF;

    // if odd, ceils half to the smallest integer number bigger than half
    const uint16_t half_power = (player1->power + 1) >> 1;
    player1->power = half_power;
    static_player.increase_power(player2, half_power);
    return OK;
}

/**
 * Represents super state according to the task's condition.
 * If action is  successful, previous two players will be replaced with the super player.
 * Also checks all input error and player's statuses and reports warnings or errors
 *
 * @param players tree of players
 * @param input read action string that will be parsed
 * @return OK if everything is ok, or particular
 * error status according to the error
 */

static action_status __super_state_action(
        player_tree* players,
        char* const input
) {
    static size_t index_of_super = 0;
    GET_TWO_PLAYERS_AND_CHECK_FIRST(player1, player2, players, input);

#ifdef __DEBUG
    static_player.debug(*player1);
    static_player.debug(*player2);
    putchar('\n');
#endif

    const uint8_t team = player1->team_number;

    if (team != player2->team_number)
        return WRONG_TEAM;

    if (player1 == player2)
        return SUPER_SELF;

    const uint16_t power1 = player1->power;
    const uint16_t power2 = player2->power;

    char name[100];
    sprintf(name, "S_%lu", index_of_super++);
    const uint16_t sum_power = power1 + power2;

    static_player_tree.insert(players, __player_new_unchecked(name, team, MIN_NUMBER(MAX_POWER, sum_power), 1));
    static_player_tree.remove(players, player1->name);
    static_player_tree.remove(players, player2->name);

    return OK;
}

// Runs the state according to the read action
// @param status: status of the parsed action
// or INPUT_ERROR if parsing is not successful

static action_status __state_machine_parse_and_run(
        const char* const action,
        player_tree* players,
        char* const input
);

static const struct {
    /** All possible states (attack, flip visibility, heal, super) */

    action_status (*states[4])(
            player_tree* players,
            char* const input
    );

    /**
     * Runs the state according to the read action
     * @param status of the parsed action
     * or INPUT_ERROR if parsing is not successful
     */

    action_status (*parse_and_run)(
            const char* const action,
            player_tree* const players,
            char* const input
    );
} state_machine = {
        .states = {
                __attack_state_action,
                __flip_visibility_state_action,
                __heal_state_action,
                __super_state_action
        },
        .parse_and_run = __state_machine_parse_and_run
};

// Runs the state according to the read action
// @param status: status of the parsed action
// or INPUT_ERROR if parsing is not successful

static action_status __state_machine_parse_and_run(
        const char* const action,
        player_tree * players,
        char* const input
) {
    if (strcmp(action, ATTACK) == 0)
        return state_machine.states[0](players, input);

    if (strcmp(action, FLIP_VISIBILITY) == 0)
        return state_machine.states[1](players, input);

    if (strcmp(action, HEAL) == 0)
        return state_machine.states[2](players, input);

    if (strcmp(action, SUPER) == 0)
        return state_machine.states[3](players, input);

    return INPUT_ERROR;
}

int main() {
    // Initializing all variables that can be freed / closed
    // in order to have correct panic with goto

    char** magician_names = NULL;
    player_tree* const players = static_player_tree.allocate();
    FILE* const fin = fopen("input.txt", "r");
    FILE* fout = fopen("output.txt", "w");

    if (fin == NULL) {
        INVALID_INPUT_PANIC(fout);
    }

    size_t number_of_teams = 0;
    READ_NUMBER_OR_PANIC(fin, "%lu", number_of_teams, fout);

    if (number_of_teams == 0 || number_of_teams > 10) {
        INVALID_INPUT_PANIC(fout);
    }

    // All club heads are stored as strings in heap
    magician_names = malloc(number_of_teams * sizeof(char*));

    for (char** magician_name = magician_names; magician_name != magician_names + number_of_teams; ++magician_name) {
        if (feof(fin)) {
            INVALID_INPUT_PANIC(fout);
        }

        char name[100];
        READ_AND_CHECK_NAME_OR_PANIC(name, fin, fout);

        // Checking if there is already magician with a given name
        const size_t cur_index = magician_name - magician_names;

        for (char** mg_name = magician_names; mg_name != magician_names + cur_index; ++mg_name)
            if (strcmp(*mg_name, name) == 0) { INVALID_INPUT_PANIC(fout); }

        *magician_name = strdup(name);
    }

    size_t number_of_players = 0;
    READ_NUMBER_OR_PANIC(fin, "%lu", number_of_players, fout);

    if (number_of_players < number_of_teams || number_of_players > 100) {
        INVALID_INPUT_PANIC(fout);
    }

    for (size_t i = 0; i < number_of_players; ++i) {
        if (feof(fin)) {
            INVALID_INPUT_PANIC(fout);
        }

        // All input checks of player's fields
        // are done in static_player.new()

        char name[100];
        READ_AND_CHECK_NAME_OR_PANIC(name, fin, fout);

        uint16_t team_number = 0;
        READ_NUMBER_OR_PANIC(fin, "%hu", team_number, fout);

        uint16_t power = 0;
        READ_NUMBER_OR_PANIC(fin, "%hu", power, fout);

        char visibility[100];
        READ_AND_TRIM_OR_PANIC(visibility, fin, fout);

        player* const player = static_player.new(name, team_number, power, visibility, number_of_teams);

        if (player == NULL) {
            // Input errors
            INVALID_INPUT_PANIC(fout);
        }

        // Checks if the player is already exists.
        // If not, successfully added to the tree

        if (!static_player_tree.insert(players, player)) {
            INVALID_INPUT_PANIC(fout);
        }
    }

    uint16_t action_amount = 0;

    for (; action_amount < 1000; ++action_amount) {
        char input[100], action[100];

        // Check if it's either the end of file or incorrect input
        if (fgets(input, 100, fin) == NULL) {
            // If EOF, stops reading and sums up the results
            if (fgetc(fin) == EOF)
                break;

            INVALID_INPUT_PANIC(fout);
        }

        TRIM_STRING_END(input);

        if (sscanf(input, "%s", action) != 1) {
            INVALID_INPUT_PANIC(fout);
        }

        TRIM_STRING_END(action);

#ifdef __DEBUG
        printf("NEW STATE: %s\n\n", action);

        puts("BEFORE:");
        static_player_tree.debug(players);
#endif

        const action_status status = state_machine.parse_and_run(action, players, input);

        if (status == INPUT_ERROR) {
            INVALID_INPUT_PANIC(fout);
        } else if (status == PLAYER_IS_INVISIBLE) {
            fputs("This player can't play\n", fout);
        } else if (status == PLAYER_IS_FROZEN) {
            fputs("This player is frozen\n", fout);
        } else if (status == WRONG_TEAM) {
            fputs("Both players should be from the same team\n", fout);
        } else if (status == HEAL_SELF) {
            fputs("The player cannot heal itself\n", fout);
        } else if (status == SUPER_SELF) {
            fputs("The player cannot do super action with itself\n", fout);
        } else if (status != OK) {
#ifdef __DEBUG
            fprintf(stderr, "Illegal status: %u", status);
            goto FREE_AND_EXIT;
#endif
        }
    }

#ifdef __DEBUG
    puts("AFTER:");
    static_player_tree.debug(players);
#endif

    // If number of actions is already 1000,
    // but there is still some data in file

    if (fgetc(fin) != EOF) {
        INVALID_INPUT_PANIC(fout);
    }

    // If we have only one team, there is no need
    // of any accumulation of player's powers

    if (number_of_teams == 1) {
        fprintf(
                fout,
                "The chosen wizard is %s\n",
                *magician_names
        );

        goto FREE_AND_EXIT;
    }

    // number_of_teams > 1

    // Initializing all team entities
    team* const teams = malloc(number_of_teams * sizeof(team));

    for (size_t i = 0; i < number_of_teams; ++i) {
        teams[i].power = 0;
        teams[i].number = i;
    }

    static_player_tree.collect_power(players, teams);

    // Sorting all teams by their power in descending order
    qsort(teams, number_of_teams, sizeof(team), team_cmp);

#ifdef __DEBUG
    for (team* t = teams; t != teams + number_of_teams; ++t)
        printf("%d: %d\n", t->number, t->power);
#endif

    if (teams->power == teams[1].power)
        fputs("It's a tie\n", fout);
    else
        fprintf(
                fout,
                "The chosen wizard is %s\n",
                magician_names[teams->number]
        );

    FREE_AND_EXIT: // Freeing all allocated memory and closing all files
    if (magician_names != NULL)
        free(magician_names);

    static_player_tree.free(players);

    fclose(fin);
    fclose(fout);
    return 0;
}
