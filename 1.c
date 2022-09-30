#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <ctype.h>

/** Comparator for the strings */

int cmp(const void* const p1, const void* const p2) {
    const char* const str1 = *((const char** const) p1);
    const char* const str2 = *((const char** const) p2);
    return strcmp(str1, str2);
}

/**
 * Merge sort implementation
 * @param _arr array to sort
 * @param size_of_elem size of element in bytes
 * @param left_border start point of sorting
 * @param right_border end point of sorting
 * @param cmp comparator of sorting
 * @see merge_sort
 */

void __merge_sort(
        void* const _arr,
        const size_t size_of_elem,
        const size_t left_border,
        const size_t right_border,
        const int (*cmp) (const void* const, const void* const)
) {
    // Borders are the same, nothing to sort
    if (left_border == right_border)
        return;

    // Cast to byte pointer
    int8_t* const arr = _arr;

    // Midpoint
    const size_t mid = (left_border + right_border) >> 1;

    __merge_sort(arr, size_of_elem, left_border, mid, cmp);     // Sorting right fragment of the array
    __merge_sort(arr, size_of_elem, mid + 1, right_border, cmp); // Sorting left fragment the array

    // Sorted holder of elems from both fragments
    int8_t* const sort_holder = malloc(right_border * size_of_elem + 1);

    const size_t holder_len = right_border - left_border + 1;
    const void* const out_of_holder_ptr = sort_holder + (holder_len * size_of_elem);
    size_t i = left_border, q = mid + 1; // iterator indices for fragments

    for (int8_t* holder_elem = sort_holder; holder_elem != out_of_holder_ptr; holder_elem += size_of_elem) {
        const void* const i_elem = arr + i * size_of_elem;
        const void* const q_elem = arr + q * size_of_elem;

        // Example:
        // I: [i1, i2] Q: [q1, q2]
        // Holder: [min(i1, q1) = i1]
        // Holder: [i1, min(i2, q1) = q1]
        // Holder: [i1, q1, min(i2, q2) = q2]
        // Holder: [i1, q1, q2, i2]

        if (q > right_border || i <= mid && cmp(i_elem, q_elem) < 0) {
            memcpy(holder_elem, arr + i * size_of_elem, size_of_elem);
            i++;
        } else {
            memcpy(holder_elem, arr + q * size_of_elem, size_of_elem);
            q++;
        }
    }

    // Copying sorted holder to the initial array
    for (int step = 0; step < right_border - left_border + 1; step++)
        memcpy(arr + (left_border + step) * size_of_elem, sort_holder + step * size_of_elem, size_of_elem);

    free(sort_holder);
}

/**
 * Merge sort algorithm
 * @param arr array to sort
 * @param size size of array
 * @param size_of_elem size of element in bytes
 * @param cmp comparator of sorting
 */

void merge_sort(
        void* const arr,
        const size_t size,
        const size_t size_of_elem,
        const int (*cmp) (const void* const, const void* const)
) { __merge_sort(arr, size_of_elem, 0, size - 1, cmp); }

/** Panics about input error and stops program */

void incorrect_input_panic() {
    FILE* out = fopen("output.txt", "w");
    fputs("Error in the input.txt", out);
    fclose(out);
    exit(0);
}

/**
 * Trims string's end by removing whitespaces
 * @param str string to trim
 */

void trim_string(char* const str) {
    str[strcspn(str, "\n")] = '\0';
}

/**
 * Checks if string satisfies the condition
 * @param str string to check
 * @returns 1 if it is correct, 0 otherwise
 */

int is_correct_string(const char* const str) {
    // Checks if the first letter is a symbol
    if (!isalpha(*str))
        return 0;

    // Checks if the first letter is an uppercase
    if (*str != toupper(*str))
        return 0;

    const size_t len = strlen(str);

    for (const char* ptr = str + 1; ptr != str + len; ptr++) {
        // Checks if the letter is a symbol
        if (!isalpha(*ptr))
            return 0;

        // Checks if the letter is a lowercase
        if (*ptr != tolower(*ptr))
            return 0;
    }

    return 1;
}

int main() {
    // Opening input file
    FILE* in = fopen("input.txt", "r");

    // Checking if the file exists
    if (in == NULL)
        incorrect_input_panic();

    // Reading n
    int n = 0;

    // Checking that n is present
    if (!fscanf(in, "%d\n", &n))
        incorrect_input_panic();

    // Checking n bounds
    if (n > 100 || n < 1)
        incorrect_input_panic();

    // Allocating memory for the n names
    char** const arr = malloc(n * sizeof(char*));

    // Reading n names
    for (char** ptr = arr; ptr != arr + n; ptr++) {
        // Checking if input is not over
        if (feof(in))
            incorrect_input_panic();

        // Reading name from file
        char name[200];

        if (fgets(name, 200, in) == NULL)
            incorrect_input_panic();

        // Trimming string's end
        trim_string(name);

        // Checking if string is correct
        if (!is_correct_string(name))
            incorrect_input_panic();

        // Allocating memory for the name
        const size_t len = strlen(name);
        *ptr = malloc(len + 1); // +1 for the '\0' at the end

        // Copying bytes to the pointer
        memcpy(*ptr, name, len);

        // Null terminator at the end of the C string
        (*ptr)[len] = '\0';
    }

    // Check if it is the end of the input
    if (fgetc(in) != EOF)
        incorrect_input_panic();

    // Closing input file
    fclose(in);

    // Sorting the array
    merge_sort(arr, n, sizeof(char*), cmp);

    // Opening output file
    FILE* out = fopen("output.txt", "w");

    // Printing all names to out file
    for (char** ptr = arr; ptr != arr + n; ptr++)
        fprintf(out, "%s\n", *ptr);

    // Closing output file
    fclose(out);
    return 0;
}
