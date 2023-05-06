import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Main class and start point of the program */

public final class OptimizedDrawingApp {
    public static void main(final String[] args) {
        final var resources = new Resources();
        final var line = new Line(Color.BLUE, Color.BLACK, 0, 0, 0, 1);
        final var rect = new Rectange(Color.BLUE, Color.BLACK, 0, 0, 0, 1, 2);
        final var trg = new Triangle(Color.BLUE, Color.BLACK, 0, 0, 0, 1, 2, 3);
        final var circ = new Circle(Color.BLUE, Color.BLACK, 0, 0, 0, 2);

        resources.draw(line, 1, 2);
        resources.draw(line, 3, 4);

        resources.draw(rect, 5, 6);
        resources.export(rect);
        resources.draw(rect, 7, 8);

        resources.export(trg);
        resources.draw(trg, 9, 10);

        resources.draw(circ, 1, 2);
        resources.draw(circ, 3, 4);
        resources.export(circ);
        resources.draw(circ, 9, 10);
    }
}

/** Abstract entity for all shapes */

abstract class Shape {

    @NotNull
    public final Color fillColor;

    @NotNull
    public final Color borderColor;

    public final double borderThickness;
    public final double coordinateX;
    public final double coordinateY;

    protected Shape(
            final @NotNull Color fillColor,
            final @NotNull Color borderColor,
            final double borderThickness,
            final double coordinateX,
            final double coordinateY
    ) {
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final var shape = (Shape) o;
        return Double.compare(shape.borderThickness, borderThickness) == 0
                && Double.compare(shape.coordinateX, coordinateX) == 0
                && Double.compare(shape.coordinateY, coordinateY) == 0
                && Objects.equals(fillColor, shape.fillColor)
                && Objects.equals(borderColor, shape.borderColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fillColor, borderColor, borderThickness, coordinateX, coordinateY);
    }
}

final class Rectange extends Shape {
    public double length;
    public double width;

    public Rectange(
            final @NotNull Color fillColor,
            final @NotNull Color borderColor,
            final double borderThickness,
            final double coordinateX,
            final double coordinateY,
            final double length,
            final double width
    ) {
        super(fillColor, borderColor, borderThickness, coordinateX, coordinateY);
        this.length = length;
        this.width = width;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final var rectange = (Rectange) o;
        return Double.compare(rectange.length, length) == 0 && Double.compare(rectange.width, width) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), length, width);
    }

    @Override
    public @NotNull String toString() { return String.format("Rectangle{length=%.1f, width=%.1f}", length, width); }

    /**
     * Some unique method that cannot be inherited
     * and will be applied in the Visitor
     */

    public void drawRectangle(final double x, final double y) {
        System.out.printf("Drawing rectangle: %s at (%.1f %.1f)\n", this, x, y);
    }
}

final class Circle extends Shape {
    public double radius;

    public Circle(
            final @NotNull Color fillColor,
            final @NotNull Color borderColor,
            final double borderThickness,
            final double coordinateX,
            final double coordinateY,
            final double radius
    ) {
        super(fillColor, borderColor, borderThickness, coordinateX, coordinateY);
        this.radius = radius;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final var circle = (Circle) o;
        return Double.compare(circle.radius, radius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), radius);
    }

    @Override
    public @NotNull String toString() { return String.format("Circle{radius=%.1f}", radius); }

    /**
     * Some unique method that cannot be inherited
     * and will be applied in the Visitor
     */

    public void drawCircle(final double x, final double y) {
        System.out.printf("Drawing circle: %s at (%.1f %.1f)\n", this, x, y);
    }
}

final class Triangle extends Shape {
    public double side1;
    public double side2;
    public double side3;

    public Triangle(
            final @NotNull Color fillColor,
            final @NotNull Color borderColor,
            final double borderThickness,
            final double coordinateX,
            final double coordinateY,
            final double side1,
            final double side2,
            final double side3
    ) {
        super(fillColor, borderColor, borderThickness, coordinateX, coordinateY);
        this.side1 = side1;
        this.side2 = side2;
        this.side3 = side3;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final var triangle = (Triangle) o;
        return Double.compare(triangle.side1, side1) == 0
                && Double.compare(triangle.side2, side2) == 0
                && Double.compare(triangle.side3, side3) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), side1, side2, side3);
    }

    @Override
    public @NotNull String toString() {
        return String.format("Triangle{side1=%.1f, side2=%.1f, side3=%.1f}", side1, side2, side3);
    }

    /**
     * Some unique method that cannot be inherited
     * and will be applied in the Visitor
     */

    public void drawTriangle(final double x, final double y) {
        System.out.printf("Drawing triangle: %s at (%.1f %.1f)\n", this, x, y);
    }
}

final class Line extends Shape {
    public double length;

    public Line(
            final @NotNull Color fillColor,
            final @NotNull Color borderColor,
            final double borderThickness,
            final double coordinateX,
            final double coordinateY,
            final double length
    ) {
        super(fillColor, borderColor, borderThickness, coordinateX, coordinateY);
        this.length = length;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final var line = (Line) o;
        return Double.compare(line.length, length) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(super.hashCode(), length); }

    @Override
    public @NotNull String toString() { return String.format("Line{length=%.1f}", length); }

    /**
     * Some unique method that cannot be inherited
     * and will be applied in the Visitor
     */

    public void drawLine(final double x, final double y) {
        System.out.printf("Drawing line: %s at (%.1f %.1f)\n", this, x, y);
    }
}

/**
 * Resources initializer and drawer.
 * Implemented as both Visitor and Flyweight.
 * Stores only unique shapes and restores
 * already initialized shapes.
 * Allows to export shapes from other resources
 */

final class Resources {

    /** Map of shape's hash code and shape itself */

    @NotNull
    private final Map<Integer, Shape> shapes;

    public Resources() { shapes = new HashMap<>(); }

    /**
     * Initializes new shape and stores it to the map
     * @param shape shape that will be initialized and cashed
     */

    @NotNull
    private Shape init(final @NotNull Shape shape) {
        System.out.printf("Creating %s with hash %d\n", shape, shape.hashCode());
        shapes.put(shape.hashCode(), shape);
        return shape;
    }

    /**
     * Accesses already cashed shape
     * @param shapeHash restorable shape's hash
     */

    @NotNull
    private Shape acquire(final int shapeHash) {
        final var shape = shapes.get(shapeHash);
        System.out.printf("Using already existing shape %s with hash %d\n", shape, shape.hashCode());
        return shape;
    }

    /**
     * Exports new shape from the resource
     * outside of application's context
     * @param shape shape that will be
     * exported and stored in the map
     */

    public void export(final @NotNull Shape shape) {
        System.out.printf("Exporting %s with hash %d\n", shape, shape.hashCode());

        if (shapes.put(shape.hashCode(), shape) != null)
            System.out.println("Shape is updated");
    }

    /**
     * Gets shape by either initialization or
     * by using already cashed instance.
     * Then draws acquired shape at given coordinates
     */

    public void draw(final @NotNull Rectange rectange, final double x, final double y) {
        final var shapeHash = rectange.hashCode();
        final var drawable = shapes.containsKey(shapeHash) ? acquire(shapeHash) : init(rectange);
        final var drawableRect = (Rectange) drawable;
        drawableRect.drawRectangle(x, y);
    }

    /**
     * Gets shape by either initialization or
     * by using already cashed instance.
     * Then draws acquired shape at given coordinates
     */

    public void draw(final @NotNull Circle circle, final double x, final double y) {
        final var shapeHash = circle.hashCode();
        final var drawable = shapes.containsKey(shapeHash) ? acquire(shapeHash) : init(circle);
        final var drawableCirc = (Circle) drawable;
        drawableCirc.drawCircle(x, y);
    }

    /**
     * Gets shape by either initialization or
     * by using already cashed instance.
     * Then draws acquired shape at given coordinates
     */

    public void draw(final @NotNull Triangle triangle, final double x, final double y) {
        final var shapeHash = triangle.hashCode();
        final var drawable = shapes.containsKey(shapeHash) ? acquire(shapeHash) : init(triangle);
        final var drawableTrg = (Triangle) drawable;
        drawableTrg.drawTriangle(x, y);
    }

    /**
     * Gets shape by either initialization or
     * by using already cashed instance.
     * Then draws acquired shape at given coordinates
     */

    public void draw(final @NotNull Line line, final double x, final double y) {
        final var shapeHash = line.hashCode();
        final var drawable = shapes.containsKey(shapeHash) ? acquire(shapeHash) : init(line);
        final var drawableLn = (Line) drawable;
        drawableLn.drawLine(x, y);
    }
}
