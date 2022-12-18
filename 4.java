import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Main {
    private static final int MIN_NUMBER_OF_PIECES = 2;

    private static Board chessBoard;

    /**
     * Tries to parse string and return {@link Integer} if it's satisfies the predicate
     * @param string string to parse
     * @param predicate checks if value is correct
     * @param exception exception to throw if something is wrong
     * @throws ChessException parsed value doesn't satisfy the predicate or there are IO errors
     * @return parsed integer
     */

    private static int parseIntOrThrow(
            final String string,
            final Predicate<Integer> predicate,
            final ChessException exception
    ) throws ChessException {
        final int integer;

        try {
            integer = Integer.parseInt(string);
        } catch (final NumberFormatException e) {
            throw exception;
        }

        if (!predicate.test(integer))
            throw exception;

        return integer;
    }

    /**
     * Tries to read {@link Integer} and parse it with {@link Main#parseIntOrThrow(String, Predicate, ChessException)}
     * @param reader {@link BufferedReader} that reads the string
     * @param predicate checks if value is correct
     * @param exception exception to throw if something is wrong
     * @throws ChessException parsed value doesn't satisfy the predicate or there are IO errors
     * @return parsed integer
     * @see Main#parseIntOrThrow(String, Predicate, ChessException)
     */

    private static int readIntOrThrow(
            final BufferedReader reader,
            final Predicate<Integer> predicate,
            final ChessException exception
    ) throws ChessException, IOException {
        return parseIntOrThrow(reader.readLine(), predicate, exception);
    }

    public static void main(final String[] args) throws IOException {
        try (final PrintWriter writer = new PrintWriter(new FileWriter("output.txt"))) {
            try (final BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
                // Reading board size and checking if it is in bounds
                // Throwing exception otherwise

                final int boardSize = readIntOrThrow(
                        reader,
                        it -> it >= Board.MIN_LENGTH && it <= Board.MAX_LENGTH,
                        new InvalidBoardSizeException()
                );

                // Creating a chess board
                chessBoard = new Board(boardSize);

                // Calculating board area to check if number of pieces is in bounds
                final int boardArea = boardSize * boardSize;

                // Reading the number of pieces and checking if it is in bounds
                // Throwing exception otherwise

                final int numberOfPieces = readIntOrThrow(
                        reader,
                        it -> it >= MIN_NUMBER_OF_PIECES && it <= boardArea,
                        new InvalidNumberOfPiecesException()
                );

                // Checking if there are only 2 kings: black and white
                boolean isWhiteKingPresent = false, isBlackKingPresent = false;

                // Storing pieces into array to improve the performance of board
                // (This way we can use HashMap instead of LinkedHashMap)
                final ChessPiece[] pieces = new ChessPiece[numberOfPieces];

                // Reading and parsing pieces, storing in both chess board and array
                // Throwing exceptions if there are some errors

                for (int i = 0; i < numberOfPieces; ++i) {
                    final String[] inp = reader.readLine().split(" ");

                    if (inp.length != 4)
                        throw new InvalidInputException();

                    final PieceColor color = PieceColor.parseOrThrow(inp[1]);

                    final int x = parseIntOrThrow(
                            inp[2],
                            it -> it >= 1 && it <= boardSize,
                            new InvalidPiecePositionException()
                    );

                    final int y = parseIntOrThrow(
                            inp[3],
                            it -> it >= 1 && it <= boardSize,
                            new InvalidPiecePositionException()
                    );

                    final ChessPiece piece = ChessPiece.parseOrThrow(
                            inp[0],
                            new PiecePosition(x, y),
                            color
                    );

                    // Check the King condition
                    // If there is already one king with same color, throw exception
                    // Otherwise change the particular flag

                    if (piece instanceof King) {
                        if (piece.getColor() == PieceColor.BLACK) {
                            if (isBlackKingPresent) throw new InvalidGivenKingsException();
                            isBlackKingPresent = true;
                        } else {
                            if (isWhiteKingPresent) throw new InvalidGivenKingsException();
                            isWhiteKingPresent = true;
                        }
                    }

                    // Store piece into board and array
                    chessBoard.addPiece(piece);
                    pieces[i] = piece;
                }

                // If it is not EOF, throw error
                if (reader.read() != -1)
                    throw new InvalidNumberOfPiecesException();

                // Throwing error if there are not 2 kings with different colors
                if (!isBlackKingPresent || !isWhiteKingPresent)
                    throw new InvalidGivenKingsException();

                // Printing moves and captures info of every piece
                for (final ChessPiece piece : pieces)
                    writer.printf(
                            "%d %d\n",
                            chessBoard.getPiecePossibleMovesCount(piece),
                            chessBoard.getPiecePossibleCapturesCount(piece)
                    );
            } catch (final IOException e) {
                // If there is an IO error, printing invalid input error
                writer.println(new InvalidInputException().getMessage());
            } catch (final Exception e) {
                // If there is another error, printing its message
                writer.println(e.getMessage());
            }
        }
    }

    @Override
    public String toString() { return chessBoard.toString(); }
}

/** Represents piece position on board */

final class PiecePosition {
    /** Column's number */
    private final int x;

    /** Row's number */
    private final int y;

    /**
     * Constructs position from given coordinates
     * @param onX column's number
     * @param onY row's number
     */

    public PiecePosition(final int onX, final int onY) {
        x = onX;
        y = onY;
    }

    /**
     * Does exactly what it should do.
     * BTW, completely useless cause it's final
     */

    public int getX() { return x; }

    /**
     * Does exactly what it should do.
     * BTW, completely useless cause it's final
     */

    public int getY() { return y; }

    /**
     * Checks if position is correct (in range 1..boardSize)
     * @param boardSize board size or maximum bound of x and x
     * @return true if position is valid
     */

    public boolean isValid(final int boardSize) { return x >= 1 && x <= boardSize && y >= 1 && y <= boardSize; }

    /** @return PiecePosition{x, y} */

    @Override
    public String toString() { return String.format("PiecePosition{%d, %d}", x, y); }

    /**
     * Checks an equality of two objects.
     * @param o object to compare
     * @return false if object is not {@link PiecePosition},
     * otherwise checks coordinates of two positions
     */

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PiecePosition position = (PiecePosition) o;
        return x == position.x && y == position.y;
    }

    /** Gets hash code for the position */

    @Override
    public int hashCode() { return Objects.hash(x, y); }
}

/**
 * Represent the colors for chess pieces
 * (only black and white)
 */

enum PieceColor {
    WHITE, BLACK;

    /**
     * Parses string and gets {@link PieceColor}
     * @param str string to parse
     * @return null if str does not match `White` or `Black`.
     * Otherwise, returns {@link PieceColor#BLACK} or {@link PieceColor#WHITE}
     */

    public static PieceColor parse(final String str) {
        if (str.equals("White")) return WHITE;
        if (str.equals("Black")) return BLACK;
        return null;
    }

    /**
     * Same as {@link PieceColor#parse(String)},
     * but throws exceptions on error
     * @param str str to parse
     * @return {@link PieceColor#BLACK} or {@link PieceColor#WHITE}
     * @throws InvalidPieceColorException if str does not match `White` or `Black`
     */

    static PieceColor parseOrThrow(final String str) throws InvalidPieceColorException {
        final PieceColor res = parse(str);
        if (res != null) return res;
        throw new InvalidPieceColorException();
    }
}

final class Utils {

    /**
     * Check if given params doesn't
     * satisfy the equation of the line (y = kx + b)
     * @return true if it is not the equation of the line
     */

    static boolean notBelongsToLineEq(
            final int x,
            final int y,
            final int k,
            final int b
    ) { return y != k * x + b; }
}

/** Function with two args */

@FunctionalInterface
interface Function2Args<F, S, R> {
    R apply(final F firstArg, final S secondArg);
}

/**
 * Represents the move state for {@link LongDistanceMovement}.
 * It is used to show the longest possible move in the direction
 * @see LongDistanceMovement
 */

interface MoveState {

    /**
     * Shows that there are not other pieces,
     * so it is possible to move to the border of the board
     */

    class Empty implements MoveState {

        /** Distance to the border */
        final int distance;

        /** @param distance distance to the border */
        Empty(final int distance) { this.distance = distance; }
    }

    /**
     * Shows that there is a piece on the way,
     * so it is possible to move not further than to this piece
     * @see SameColor
     * @see DifferentColor
     */

    abstract class NonEmpty implements MoveState {
        /** Position of the first met piece on the way */
        final PiecePosition position;

        protected NonEmpty(final PiecePosition position) { this.position = position; }

        /**
         * Shows that there is a piece with same color on the way,
         * so the maximum move length is distance - 1
         */

        static final class SameColor extends NonEmpty {
            SameColor(final PiecePosition position) { super(position); }
        }

        /**
         * Shows that there is a piece with another color on the way,
         * so the maximum move length is distance between them
         */

        static final class DifferentColor extends NonEmpty {
            DifferentColor(final PiecePosition position) { super(position); }
        }
    }
}

/** Ancestor for all movements */
interface Movement {}

/**
 * Used to represent all movements
 * with not compile-time known move length.
 * Provides a specific algorithm to count the number of moves and captures
 * @see BishopMovement
 * @see RookMovement
 */

interface LongDistanceMovement extends Movement {

    /** Gets {@link MoveState}s for all possible ways  */

    @FunctionalInterface
    interface GetFirstMetPositions {
        MoveState[] apply(
                final PiecePosition position,
                final PieceColor color,
                final Map<String, ChessPiece> positions,
                final int boardSize
        );
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @param calcDistanceExcludingColor how to find the distance
     * if there is a piece on the way (color doesn't matter)
     * @param getFirstMetPositions how to get first positions
     * of pieces on the way (if there are any)
     * @return the number of all possible moves
     * @see GetFirstMetPositions
     */

    default int getLongDistanceMovesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize,
            final Function<PiecePosition, Integer> calcDistanceExcludingColor,
            final GetFirstMetPositions getFirstMetPositions
    ) {
        return Arrays
                .stream(getFirstMetPositions.apply(position, color, positions, boardSize))
                .map(moveState -> {
                    if (moveState instanceof MoveState.NonEmpty) {
                        final MoveState.NonEmpty nonEmpty = (MoveState.NonEmpty) moveState;
                        return calcDistanceExcludingColor.apply(nonEmpty.position) -
                                (nonEmpty instanceof MoveState.NonEmpty.SameColor ? 1 : 0);
                    } else {
                        final MoveState.Empty empty = (MoveState.Empty) moveState;
                        return empty.distance;
                    }
                })
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * Gets the number of all possible captures
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @param getFirstMetPositions how to get first positions
     * of pieces on the way (if there are any)
     * @return the number of all possible captures
     * @see GetFirstMetPositions
     */

    default int getLongDistanceCapturesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize,
            final GetFirstMetPositions getFirstMetPositions
    ) {
        return (int) Arrays
                .stream(getFirstMetPositions.apply(position, color, positions, boardSize))
                .filter(moveState -> moveState instanceof MoveState.NonEmpty.DifferentColor)
                .count();
    }
}

/** {@link LongDistanceMovement} with diagonal moves */

interface BishopMovement extends LongDistanceMovement {
    class Ext {

        /**
         * Gets {@link MoveState}s for all diagonals
         * @param position current piece's position
         * @param color piece's color
         * @param positions board
         * @param boardSize number of rows / columns of the board
         * @return all {@link MoveState}s for 4 diagonals
         */

        static MoveState[] getFirstMetPositions(
                final PiecePosition position,
                final PieceColor color,
                final Map<String, ChessPiece> positions,
                final int boardSize
        ) {
            final int x = position.getX();
            final int y = position.getY();

            // --------------------------------------------------------
            // Equation of line for diagonal /
            // --------------------------------------------------------
            // y = kx + b
            // y+1 = k(x+1) + b
            // --------------------------------------------------------
            // kx + b + 1 = kx + k + b
            // k = 1
            // b = y - x
            // --------------------------------------------------------
            // Y = X + (y-x)
            // --------------------------------------------------------

            final int k1 = 1;
            final int b1 = y - x;

            // --------------------------------------------------------
            // Equation of line for diagonal \
            // --------------------------------------------------------
            // y = kx + b
            // y-1 = k(x+1) + b
            // --------------------------------------------------------
            // kx + b - 1 = kx + k + b
            // k = -1
            // b = y + x
            // --------------------------------------------------------
            // Y = -X + (y+x)
            // --------------------------------------------------------

            final int k2 = -1;
            final int b2 = y + x;

            // 0: x > to y > (up-right)
            // 1: x > to y < (down-right)
            // 2: x < to y > (up-left)
            // 3: x < to y < (down-left)

            // Initializing array by Empty states (with distances to boundaries)

            final MoveState[] firstMetPiecePositions = new MoveState[] {
                    new MoveState.Empty(Math.min(boardSize - x, boardSize - y)),
                    new MoveState.Empty(Math.min(boardSize - x, y - 1)),
                    new MoveState.Empty(Math.min(x - 1, boardSize - y)),
                    new MoveState.Empty(Math.min(x - 1, y - 1))
            };

            // Going through all pieces on board and pushing them to states array
            // If the piece is closer to our piece than current piece in state
            // (if there is any), updates the state and proceed.
            // This way we will find the closest pieces for all diagonals,
            // and then we can calculate both the number of possible moves and captures

            positions.forEach((key, piece) -> {
                final PiecePosition pos = piece.getPosition();
                final int px = pos.getX();
                final int py = pos.getY();

                // Skip if it is the same piece
                if (px == x && py == y)
                    return;

                // Checking if piece lies on any of our piece's diagonals
                // In other words, can this piece be reached

                if (Utils.notBelongsToLineEq(px, py, k1, b1) &&
                        Utils.notBelongsToLineEq(px, py, k2, b2))
                    return;

                // To avoid duplicating, I've provided functions to update first met positions.
                // There are too many dependencies and function should have too many arguments,
                // that is why is makes sense to have an inside function.
                // Unlike in Kotlin or JS/TS, it is not possible in Java to have a normal function
                // inside the body of another function, so it is what it is.
                // Also, this example shows why there is a keyword `var` in Java 10...

                // Checks if first met positions should be updated
                // In other words, is the distance smaller than the current distance
                // @param ind - number of the diagonal (the list was provided above)
                // @param onNonEmpty - is the distance is smaller than the previous one
                // @param onNonEmpty 1 - x coordinate of the current state's position
                // @return true if it should be updated

                final Function2Args<Integer, Function<Integer, Boolean>, Boolean> shouldUpdateMetPositions = (ind, onNonEmpty) ->
                        firstMetPiecePositions[ind] instanceof MoveState.Empty ||
                                onNonEmpty.apply(((MoveState.NonEmpty) firstMetPiecePositions[ind]).position.getX());

                // Updates first met position if it should be updated,
                // in other words, is the distance smaller than the current distance
                // @param ind - number of the diagonal (the list was provided above)
                // @param onNonEmpty - is the distance is smaller than the previous one
                // @param onNonEmpty 1 - x coordinate of the current state's position
                // (the reason to use x instead of the position itself is that y - py == x - px)

                final Function2Args<Integer, Function<Integer, Boolean>, Void> updateMetPiecePositionsIfOk = (ind, onNonEmpty) -> {
                    if (shouldUpdateMetPositions.apply(ind, onNonEmpty)) {
                        final PiecePosition piecePosition = new PiecePosition(px, py);
                        firstMetPiecePositions[ind] = piece.getColor() == color ?
                                new MoveState.NonEmpty.SameColor(piecePosition) :
                                new MoveState.NonEmpty.DifferentColor(piecePosition);
                    }

                    return null;
                };

                if (px > x)
                    updateMetPiecePositionsIfOk.apply(py > y ? 0 : 1, (curX) -> px < curX);
                else
                    updateMetPiecePositionsIfOk.apply(py > y ? 2 : 3, (curX) -> px > curX);
            });

            return firstMetPiecePositions;
        }
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    default int getDiagonalMovesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        final int x = position.getX();

        return getLongDistanceMovesCount(
                position,
                color,
                positions,
                boardSize,
                firstMetPos -> Math.abs(x - firstMetPos.getX()),
                Ext::getFirstMetPositions
        );
    }

    /**
     * Gets the number of all possible captures
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    default int getDiagonalCapturesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        return getLongDistanceCapturesCount(
                position,
                color,
                positions,
                boardSize,
                Ext::getFirstMetPositions
        );
    }
}

/** {@link LongDistanceMovement} with orthogonal moves */

interface RookMovement extends LongDistanceMovement {
    class Ext {

        /**
         * Gets {@link MoveState}s for all orthogonal moves
         * @param position current piece's position
         * @param color piece's color
         * @param positions board
         * @param boardSize number of rows / columns of the board
         * @return all {@link MoveState}s for 4 orthogonal moves
         */

        static MoveState[] getFirstMetPositions(
                final PiecePosition position,
                final PieceColor color,
                final Map<String, ChessPiece> positions,
                final int boardSize
        ) {
            final int x = position.getX();
            final int y = position.getY();

            // 0: x > (right)
            // 1: x < (left)
            // 2: y > (up)
            // 3: y < (down)

            // Initializing array by Empty states (with distances to boundaries)

            final MoveState[] firstMetPiecePositions = new MoveState[] {
                    new MoveState.Empty(boardSize - x),
                    new MoveState.Empty(x - 1),
                    new MoveState.Empty(boardSize - y),
                    new MoveState.Empty(y - 1)
            };

            // Going through all pieces on board and pushing them to states array
            // If the piece is closer to our piece than current piece in state
            // (if there is any), updates the state and proceed.
            // This way we will find the closest pieces for all orthogonal moves,
            // and then we can calculate both the number of possible moves and captures

            positions.forEach((key, piece) -> {
                final PiecePosition pos = piece.getPosition();
                final int px = pos.getX();
                final int py = pos.getY();

                // Skip if piece doesn't lie on any of orthogonal
                if (px != x && py != y)
                    return;

                // Skip if it is the same piece
                if (px == x && py == y)
                    return;

                // To avoid duplicating, I've provided functions to update first met positions.
                // There are too many dependencies and function should have too many arguments,
                // that is why is makes sense to have an inside function.
                // Unlike in Kotlin or JS/TS, it is not possible in Java to have a normal function
                // inside the body of another function, so it is what it is.
                // Also, this example shows why there is a keyword `var` in Java 10...

                // Checks if first met positions should be updated
                // In other words, is the distance smaller than the current distance
                // @param ind - number of the orthogonal move (the list was provided above)
                // @param onNonEmpty - is the distance is smaller than the previous one
                // @param onNonEmpty 1 - position of the current state's position
                // @return true if it should be updated

                final Function2Args<Integer, Function<PiecePosition, Boolean>, Boolean> shouldUpdateMetPositions = (ind, onNonEmpty) ->
                        firstMetPiecePositions[ind] instanceof MoveState.Empty ||
                                onNonEmpty.apply(((MoveState.NonEmpty) firstMetPiecePositions[ind]).position);

                // Updates first met position if it should be updated,
                // in other words, is the distance smaller than the current distance
                // @param ind - number of the diagonal (the list was provided above)
                // @param onNonEmpty - is the distance is smaller than the previous one
                // @param onNonEmpty 1 - position of the current state's position

                final Function2Args<Integer, Function<PiecePosition, Boolean>, Void> updateMetPiecePositionsIfOk = (ind, onNonEmpty) -> {
                    if (shouldUpdateMetPositions.apply(ind, onNonEmpty)) {
                        final PiecePosition piecePosition = new PiecePosition(px, py);
                        firstMetPiecePositions[ind] = piece.getColor() == color ?
                                new MoveState.NonEmpty.SameColor(piecePosition) :
                                new MoveState.NonEmpty.DifferentColor(piecePosition);
                    }

                    return null;
                };

                if (py == y) {
                    if (px > x) // updates right
                        updateMetPiecePositionsIfOk.apply(0, curPosition -> px < curPosition.getX());
                    else        // updates left
                        updateMetPiecePositionsIfOk.apply(1, curPosition -> px > curPosition.getX());
                } else {
                    if (py > y) // updates up
                        updateMetPiecePositionsIfOk.apply(2, curPosition -> py < curPosition.getY());
                    else        // updates down
                        updateMetPiecePositionsIfOk.apply(3, curPosition -> py > curPosition.getY());
                }
            });

            return firstMetPiecePositions;
        }
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    default int getOrthogonalMovesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        final int x = position.getX();
        final int y = position.getY();

        return getLongDistanceMovesCount(
                position,
                color,
                positions,
                boardSize,
                firstMetPos -> Math.max(
                        Math.abs(x - firstMetPos.getX()),
                        Math.abs(y - firstMetPos.getY())
                ),
                Ext::getFirstMetPositions
        );
    }

    /**
     * Gets the number of all possible captures
     * @param position current piece's position
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    default int getOrthogonalCapturesCount(
            final PiecePosition position,
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        return getLongDistanceCapturesCount(
                position,
                color,
                positions,
                boardSize,
                Ext::getFirstMetPositions
        );
    }
}

/**
 * {@link Movement} with compile-time constant amount of positions to move
 * @see SimpleChessPiece
 */

interface SimpleMovement extends Movement {
    /**
     * Gets all positions that piece
     * potentially can move (even if it's not possible)
     * @return the maximum number of positions to move in
     */

    PiecePosition[] getSimpleMovePositions();

    /**
     * Gets all potential positions to capture (even if it's not possible).
     * By default, it is the same as {@link SimpleMovement#getSimpleMovePositions()}
     * @return the maximum number of positions to capture
     */

    default PiecePosition[] getSimpleCapturePositions() { return getSimpleMovePositions(); }

    /**
     * Gets the number of all possible moves (including captures)
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    default int getSimpleMovesCount(
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        return Arrays.stream(getSimpleMovePositions())
                .filter(position -> position.isValid(boardSize))
                .map(position -> positions.get(position.toString()))
                .map(piece -> piece == null ? 1 : (piece.getColor() == color ? 0 : 1))
                .reduce(Integer::sum)
                .orElse(0);
    }

    /**
     * Gets the number of all possible captures
     * @param color piece's color
     * @param positions board
     * @return the number of all possible captures
     */

    default int getSimpleCapturesCount(final PieceColor color, final Map<String, ChessPiece> positions) {
        return Arrays.stream(getSimpleCapturePositions())
                .map(position -> positions.get(position.toString()))
                .map(piece -> piece == null ? 0 : (piece.getColor() == color ? 0 : 1))
                .reduce(Integer::sum)
                .orElse(0);
    }

}

/**
 * Ancestor for all chess pieces.
 * Should be sealed class, but there are none of them in Java 8...
 * BTW, this feature and many others are stolen from Kotlin :)
 */

abstract class ChessPiece implements Movement {

    /** Current position */
    protected final PiecePosition position;

    /** Piece's color */
    protected final PieceColor color;

    /**
     * Constructs piece from given position and color.
     * P.S. I don't care, it must be marked as protected.
     * It is illegal to use this constructor outside the ancestors
     */

    protected ChessPiece(final PiecePosition piecePosition, final PieceColor pieceColor) {
        position = piecePosition;
        color = pieceColor;
    }

    /**
     * Constructs the {@link ChessPiece} from the given string
     * @param name name of the piece to construct
     * @param position position of the constructable piece
     * @param color color of the constructable piece
     * @throws InvalidPieceNameException if the name is not in set:
     * {`Pawn`, `King`, `Knight`, `Rook`, `Queen`, `Bishop`}
     */

    static ChessPiece parseOrThrow(
            final String name,
            final PiecePosition position,
            final PieceColor color
    ) throws InvalidPieceNameException {
        if (name.equals("Pawn")) return new Pawn(position, color);
        if (name.equals("King")) return new King(position, color);
        if (name.equals("Knight")) return new Knight(position, color);
        if (name.equals("Rook")) return new Rook(position, color);
        if (name.equals("Queen")) return new Queen(position, color);
        if (name.equals("Bishop")) return new Bishop(position, color);
        throw new InvalidPieceNameException();
    }

    /**
     * Does exactly what it should do.
     * BTW, completely useless cause it's final
     */

    public final PiecePosition getPosition() { return position; }

    /**
     * Does exactly what it should do.
     * BTW, completely useless cause it's final
     */

    public final PieceColor getColor() { return color; }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    public abstract int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize);

    /**
     * Gets the number of all possible captures
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    public abstract int getCapturesCount(final Map<String, ChessPiece> positions, final int boardSize);
}

/**
 * Ancestor for all chess pieces with
 * compile-time constant maximum number of moves
 * @see SimpleMovement
 */

abstract class SimpleChessPiece extends ChessPiece implements SimpleMovement {

    /** Constructs piece from given position and color */

    protected SimpleChessPiece(final PiecePosition piecePosition, final PieceColor pieceColor) {
        super(piecePosition, pieceColor);
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getSimpleMovesCount(color, positions, boardSize);
    }

    /**
     * Gets the number of all possible captures
     * @param positions board
     * @return the number of all possible captures
     */

    @Override
    public int getCapturesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getSimpleCapturesCount(color, positions);
    }
}

/** Knight, AKA Конь. Moves with Г trajectory */

final class Knight extends SimpleChessPiece {

    /** Constructs knight from the position and color */
    public Knight(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets all positions that piece
     * potentially can move (even if it's not possible)
     * @return the maximum number of positions to move in
     */

    @Override
    public PiecePosition[] getSimpleMovePositions() {
        final int x = position.getX();
        final int y = position.getY();

        final PiecePosition[] positionArr = new PiecePosition[8];

        // -------------------------------------------- 1 --------------------------------------------

        // . . . . .
        // . . . . .
        // . . K . .
        // . . . . .
        // . P . . .

        positionArr[0] = new PiecePosition(x - 1, y - 2);

        // -------------------------------------------- 2 --------------------------------------------

        // . P . . .
        // . . . . .
        // . . K . .
        // . . . . .
        // . . . . .

        positionArr[1] = new PiecePosition(x - 1, y + 2);

        // -------------------------------------------- 3 --------------------------------------------

        // . . . . .
        // . . . . .
        // . . K . .
        // P . . . .
        // . . . . .

        positionArr[2] = new PiecePosition(x - 2, y - 1);

        // -------------------------------------------- 4 --------------------------------------------

        // . . . . .
        // P . . . .
        // . . K . .
        // . . . . .
        // . . . . .

        positionArr[3] = new PiecePosition(x - 2, y + 1);

        // -------------------------------------------- 5 --------------------------------------------

        // . . . . .
        // . . . . .
        // . . K . .
        // . . . . .
        // . . . P .

        positionArr[4] = new PiecePosition(x + 1, y - 2);

        // -------------------------------------------- 6 --------------------------------------------

        // . . . P .
        // . . . . .
        // . . K . .
        // . . . . .
        // . . . . .

        positionArr[5] = new PiecePosition(x + 1, y + 2);

        // -------------------------------------------- 7 --------------------------------------------

        // . . . . .
        // . . . . .
        // . . K . .
        // . . . . P
        // . . . . .

        positionArr[6] = new PiecePosition(x + 2, y - 1);

        // -------------------------------------------- 8 --------------------------------------------

        // . . . . .
        // . . . . P
        // . . K . .
        // . . . . .
        // . . . . .

        positionArr[7] = new PiecePosition(x + 2, y + 1);

        // -------------------------------------------------------------------------------------------

        return positionArr;
    }
}

/** King, AKA Король. Moves on neighbor cells */

final class King extends SimpleChessPiece {

    /** Constructs the king from given position and color */
    public King(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets all positions that piece
     * potentially can move (even if it's not possible)
     * @return the maximum number of positions to move in
     */

    @Override
    public PiecePosition[] getSimpleMovePositions() {
        final int x = getPosition().getX();
        final int y = getPosition().getY();

        return new PiecePosition[] {
                new PiecePosition(x - 1, y - 1),
                new PiecePosition(x - 1, y),
                new PiecePosition(x - 1, y + 1),
                new PiecePosition(x, y - 1),
                new PiecePosition(x, y + 1),
                new PiecePosition(x + 1, y - 1),
                new PiecePosition(x + 1, y),
                new PiecePosition(x + 1, y + 1)
        };
    }
}

/** Pawn, AKA Пешка. Moves on the cell in front of her, captures 2 cell on diagonals */

final class Pawn extends SimpleChessPiece {

    /** Constructs the pawn from given position and color */
    public Pawn(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets all positions that piece
     * potentially can move (even if it's not possible)
     * @return the maximum number of positions to move in
     */

    @Override
    public PiecePosition[] getSimpleMovePositions() {
        final int x = position.getX();
        final int y = position.getY();
        return new PiecePosition[] { new PiecePosition(x, y + (color == PieceColor.WHITE ? 1 : -1)) };
    }

    /**
     * Gets all potential positions to capture (even if it's not possible).
     * @return the maximum number of positions to capture
     */

    @Override
    public PiecePosition[] getSimpleCapturePositions() {
        final int x = position.getX();
        final int y = position.getY();

        return color == PieceColor.WHITE ? new PiecePosition[] {
                new PiecePosition(x - 1, y + 1),
                new PiecePosition(x + 1, y + 1)
        } : new PiecePosition[] {
                new PiecePosition(x - 1, y - 1),
                new PiecePosition(x + 1, y - 1)
        };
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param color piece's color
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getSimpleMovesCount(
            final PieceColor color,
            final Map<String, ChessPiece> positions,
            final int boardSize
    ) {
        final PiecePosition movePosition = getSimpleMovePositions()[0];
        if (!movePosition.isValid(boardSize)) return 0;
        return positions.get(getSimpleMovePositions()[0].toString()) == null ? 1 : 0;
    }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getSimpleMovesCount(color, positions, boardSize) + getSimpleCapturesCount(color, positions);
    }
}

/** Bishop, AKA Слон. Moves diagonally on any number of cells */

final class Bishop extends ChessPiece implements BishopMovement {

    /** Constructs the bishop from given position and color */
    public Bishop(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getDiagonalMovesCount(position, color, positions, boardSize);
    }

    /**
     * Gets the number of all possible captures
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    @Override
    public int getCapturesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getDiagonalCapturesCount(position, color, positions, boardSize);
    }
}

/** Rook, AKA Ладья. Moves orthogonally on any number of cells */

final class Rook extends ChessPiece implements RookMovement {

    /** Constructs the rook from given position and color */
    public Rook(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getOrthogonalMovesCount(position, color, positions, boardSize);
    }

    /**
     * Gets the number of all possible captures
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    @Override
    public int getCapturesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        return getOrthogonalCapturesCount(position, color, positions, boardSize);
    }
}

/** Queen, AKA Королева. Moves as all pieces altogether */

final class Queen extends ChessPiece implements BishopMovement, RookMovement {

    /** Constructs the rook from given position and color */
    public Queen(final PiecePosition piecePosition, final PieceColor pieceColor) { super(piecePosition, pieceColor); }

    /**
     * Gets the number of all possible moves (including captures)
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible moves
     */

    @Override
    public int getMovesCount(final Map<String, ChessPiece> positions, final int boardSize) {
        final int diagonal = getDiagonalMovesCount(position, color, positions, boardSize);
        final int orthogonal = getOrthogonalMovesCount(position, color, positions, boardSize);
        return diagonal + orthogonal;
    }

    /**
     * Gets the number of all possible captures
     * @param positions board
     * @param boardSize number of rows / columns of the board
     * @return the number of all possible captures
     */

    @Override
    public int getCapturesCount(Map<String, ChessPiece> positions, int boardSize) {
        final int diagonal = getDiagonalCapturesCount(position, color, positions, boardSize);
        final int orthogonal = getOrthogonalCapturesCount(position, color, positions, boardSize);
        return diagonal + orthogonal;
    }
}

/** Ancestor for all chess-related exception */

abstract class ChessException extends Exception {}

/** Threw if board size is not in [3..1000] */

final class InvalidBoardSizeException extends ChessException {
    @Override
    public String getMessage() { return "Invalid board size"; }
}

/** Threw if number of pieces is not in [2..boardArea] */

final class InvalidNumberOfPiecesException extends ChessException {
    @Override
    public String getMessage() { return "Invalid number of pieces"; }
}

/** Threw if the name is not in set {`Pawn`, `King`, `Knight`, `Rook`, `Queen`, `Bishop`} */

final class InvalidPieceNameException extends ChessException {
    @Override
    public String getMessage() { return "Invalid piece name"; }
}

/** Threw if the color is neither `Black`, not `White` */

final class InvalidPieceColorException extends ChessException {
    @Override
    public String getMessage() { return "Invalid piece color"; }
}

/** Threw if the position is invalid or occupied by another cell */

final class InvalidPiecePositionException extends ChessException {
    @Override
    public String getMessage() { return "Invalid piece position"; }
}

/** Threw if there are not exactly two kings: black and white */

final class InvalidGivenKingsException extends ChessException {
    @Override
    public String getMessage() { return "Invalid given Kings"; }
}

/** Threw if there are some IO error */

final class InvalidInputException extends ChessException {
    @Override
    public String getMessage() { return "Invalid input"; }
}

/**
 * Chess Board itself.
 * BTW, it is better to mark it as singleton
 * and use a dependency injection framework
 */

final class Board {
    static final int MIN_LENGTH = 3;
    static final int MAX_LENGTH = 1000;

    /** Stores all pieces by their positions */

    private final Map<String, ChessPiece> positionsToPieces = new HashMap<>();

    /** Number of rows (columns) of the board */

    private final int size;

    /** Constructs board by the number of rows (columns) */

    public Board(final int boardSize) { size = boardSize; }

    /**
     * Gets the number of all possible moves (including captures)
     * @param piece piece to get the number of moves
     * @return the number of all possible moves for the piece
     */

    public int getPiecePossibleMovesCount(final ChessPiece piece) {
        return piece.getMovesCount(positionsToPieces, size);
    }

    /**
     * Gets the number of all possible captures
     * @param piece piece to get the number of captures
     * @return the number of all possible captures
     */

    public int getPiecePossibleCapturesCount(final ChessPiece piece) {
        return piece.getCapturesCount(positionsToPieces, size);
    }

    /**
     * Adds piece to the board.
     * @param piece piece to add
     * @throws InvalidPiecePositionException
     * if the position is already assigned to another piece
     */

    public void addPiece(final ChessPiece piece) throws InvalidPiecePositionException {
        final PiecePosition position = piece.getPosition();

        if (getPiece(position) != null)
            throw new InvalidPiecePositionException();

        positionsToPieces.put(position.toString(), piece);
    }

    /** Gets piece by its position */

    public ChessPiece getPiece(final PiecePosition position) {
        return positionsToPieces.get(position.toString());
    }
}
