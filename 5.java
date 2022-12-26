import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    private static final int MIN_GRASS = 0;
    private static final int MAX_GRASS = 100;

    private static final int MIN_ANIMALS = 1;
    private static final int MAX_ANIMALS = 20;

    private static final int ANIMAL_PARAMS_NUMBER = 4;

    private static final int MIN_WEIGHT = 5;
    private static final int MAX_WEIGHT = 200;

    private static final int MIN_SPEED = 5;
    private static final int MAX_SPEED = 60;

    private static final int MIN_ENERGY = 0;
    private static final int MAX_ENERGY = 100;

    /**
     * Reads int and checks its bounds
     * @param reader reader itself
     * @param min lower bound
     * @param max upper bound
     * @return read integer
     * @throws InvalidInputsException if number
     * is not parsed successfully or goes out of bounds
     */

    private static int readIntOrThrow(
            final BufferedReader reader,
            final int min,
            final int max
    ) throws InvalidInputsException {
        try {
            final int inp = Integer.parseInt(reader.readLine());

            if (inp >= min && inp <= max) {
                return inp;
            }

            throw new InvalidInputsException();
        } catch (final IOException | NumberFormatException ignored) {
            throw new InvalidInputsException();
        }
    }

    /**
     * Reads day and checks that its in bounds [{@link #MIN_DAYS}..{@link #MAX_DAYS}]
     * @param reader reader itself
     * @return number of days
     * @throws InvalidInputsException if parse wasn't successful or not in bounds
     * @see #readIntOrThrow(BufferedReader, int, int)
     */

    private static int readDaysOrThrow(final BufferedReader reader) throws InvalidInputsException {
        return readIntOrThrow(reader, MIN_DAYS, MAX_DAYS);
    }

    /**
     * Reads amount of grass, checks its bounds and returns new {@link Field}
     * @param reader reader itself
     * @return new {@link Field} from read amount of grass
     * @throws GrassOutOfBoundsException if amount of grass is out of bounds
     * @throws InvalidInputsException'if there are some IO errors
     */

    private static Field readFieldOrThrow(final BufferedReader reader) throws InvalidDataException {
        try {
            final float grassAmount = Float.parseFloat(reader.readLine());

            if (grassAmount >= MIN_GRASS && grassAmount <= MAX_GRASS) {
                return new Field(grassAmount);
            }

            throw new GrassOutOfBoundsException();
        } catch (final IOException | NumberFormatException ignored) {
            throw new InvalidInputsException();
        }
    }

    /**
     * Reads number of animals and checks its bounds
     * @param reader reader itself
     * @return number of animals
     * @throws InvalidInputsException if parsing wasn't successful or not in bounds
     */

    private static int readAnimalsNumberOrThrow(final BufferedReader reader) throws InvalidInputsException {
        return readIntOrThrow(reader, MIN_ANIMALS, MAX_ANIMALS);
    }

    /**
     * Parses animal's numeric param (weight, speed, energy) and checks its bounds
     * @param input string to parse
     * @param min lower bounds
     * @param max upper bound
     * @param outOfBoundsExceptionClass exception's class to instantiate new exception
     * @return parsed animal's numeric param
     * @throws InvalidInputsException if there are some IO errors
     * @throws InvalidDataException if params is out of bounds
     */

    private static float parseAnimalNumericParamOrThrow(
            final String input,
            final int min,
            final int max,
            final Class<? extends InvalidDataException> outOfBoundsExceptionClass
    ) throws InvalidDataException {
        try {
            final float param = Float.parseFloat(input);

            if (param >= min && param <= max) {
                return param;
            }

            throw outOfBoundsExceptionClass.newInstance();
        } catch (final NumberFormatException | InstantiationException | IllegalAccessException ignored) {
            throw new InvalidInputsException();
        }
    }

    /**
     * Parses animal's weight and checks its bounds [{@link #MIN_WEIGHT}..{@link #MAX_WEIGHT}]
     * @param input string to parse
     * @return animal's weight
     * @throws InvalidInputsException if there are some IO errors
     * @throws InvalidDataException if params is out of bounds
     * @see #parseAnimalNumericParamOrThrow(String, int, int, Class)
     */

    private static float parseAnimalWeightOrThrow(final String input) throws InvalidDataException {
        return parseAnimalNumericParamOrThrow(input, MIN_WEIGHT, MAX_WEIGHT, WeightOutOfBoundsException.class);
    }

    /**
     * Parses animal's speed and checks its bounds [{@link #MIN_SPEED}..{@link #MAX_SPEED}]
     * @param input string to parse
     * @return animal's speed
     * @throws InvalidInputsException if there are some IO errors
     * @throws InvalidDataException if params is out of bounds
     * @see #parseAnimalNumericParamOrThrow(String, int, int, Class)
     */

    private static float parseAnimalSpeedOrThrow(final String input) throws InvalidDataException {
        return parseAnimalNumericParamOrThrow(input, MIN_SPEED, MAX_SPEED, SpeedOutOfBoundsException.class);
    }

    /**
     * Parses animal's energy and checks its bounds [{@link #MIN_ENERGY}..{@link #MAX_ENERGY}]
     * @param input string to parse
     * @return animal's energy
     * @throws InvalidInputsException if there are some IO errors
     * @throws InvalidDataException if params is out of bounds
     * @see #parseAnimalNumericParamOrThrow(String, int, int, Class)
     */

    private static float parseAnimalEnergyOrThrow(final String input) throws InvalidDataException {
        return parseAnimalNumericParamOrThrow(input, MIN_ENERGY, MAX_ENERGY, EnergyOutOfBoundsException.class);
    }

    /**
     * Reads string and parses animal from it
     * @param reader reader itself
     * @return new animal (either {@link Boar}, {@link Lion} or {@link Zebra})
     * @throws InvalidInputsException if there are some IO errors
     * @throws InvalidNumberOfAnimalParametersException
     * if input string has not {@link #ANIMAL_PARAMS_NUMBER} params
     * @throws InvalidDataException if params is out of bounds
     * @see #parseAnimalWeightOrThrow(String)
     * @see #parseAnimalSpeedOrThrow(String)
     * @see #parseAnimalEnergyOrThrow(String)
     */

    private static Animal readAnimalOrThrow(final BufferedReader reader) throws InvalidDataException {
        try {
            final String[] input = reader.readLine().split(" ");

            if (input.length != ANIMAL_PARAMS_NUMBER) {
                throw new InvalidNumberOfAnimalParametersException();
            }

            if (input[0].equals("Boar")) {
                final float weight = parseAnimalWeightOrThrow(input[1]);
                final float speed = parseAnimalSpeedOrThrow(input[2]);
                final float energy = parseAnimalEnergyOrThrow(input[3]);
                return new Boar(weight, speed, energy);
            }

            if (input[0].equals("Lion")) {
                final float weight = parseAnimalWeightOrThrow(input[1]);
                final float speed = parseAnimalSpeedOrThrow(input[2]);
                final float energy = parseAnimalEnergyOrThrow(input[3]);
                return new Lion(weight, speed, energy);
            }

            if (input[0].equals("Zebra")) {
                final float weight = parseAnimalWeightOrThrow(input[1]);
                final float speed = parseAnimalSpeedOrThrow(input[2]);
                final float energy = parseAnimalEnergyOrThrow(input[3]);
                return new Zebra(weight, speed, energy);
            }

            throw new InvalidInputsException();
        } catch (final IOException ignored) {
            throw new InvalidInputsException();
        }
    }

    public static void main(final String[] args) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader("input.txt"))) {
            final int days = readDaysOrThrow(reader);
            final Field field = readFieldOrThrow(reader);
            final int animalsNumber = readAnimalsNumberOrThrow(reader);

            // Reading animal and adding to list if it's alive

            final List<Animal> animals = new ArrayList<>(animalsNumber);

            for (int i = 0; i < animalsNumber; ++i) {
                final Animal animal = readAnimalOrThrow(reader);

                if (animal.energy > MIN_ENERGY) {
                    animals.add(animal);
                }
            }

            // Creating state machine that handles all animals' lifecycles and field's lifecycle
            final ZooStateMachine stateMachine = new ZooStateMachine(field, animals);
            stateMachine.handleDays(days);
            stateMachine.makeSounds();
        } catch (final InvalidDataException e) {
            System.out.println(e.getMessage());
        } catch (final Exception e) {
            System.out.println(InvalidInputsException.MESSAGE);
        }
    }
}

/** Field that is eaten by {@link Herbivore}s */

final class Field {
    static final float NO_GRASS = 0.0F;
    private static final float GRASS_GROWING = 2.0F;
    private static final float MAX_GRASS = 100.0F;

    private float grassAmount;

    float getGrassAmount() {
        return grassAmount;
    }

    Field(final float grassAmount) {
        this.grassAmount = grassAmount;
    }

    /** Sets grass amount as GA * 2 or 100 if GA * 2 > 100 */
    void grassGrow() {
        if (grassAmount == NO_GRASS) return;
        grassAmount = Math.min(grassAmount * GRASS_GROWING, MAX_GRASS);
    }

    /**
     * Decreases grass amount after animal's eating
     * @param grassPortion portion for animal's eating
     */

    void decreaseGrassAmount(final float grassPortion) {
        grassAmount -= grassPortion;
    }
}

/**
 * Describes animal as thing with weight, speed and energy.
 * Provides getters and setters to access parameters
 */

interface Creature {
    float NO_ENERGY = 0.0F;
    float MAX_ENERGY = 100.0F;

    float getWeight();

    float getSpeed();

    float getEnergy();

    void setEnergy(final float energy);

    /**
     * Sets energy as current energy + given energy
     * if not greater than 100, otherwise, sets as 100
     * @param energy energy to add
     */

    default void addEnergy(final float energy) {
        setEnergy(Math.min(getEnergy() + energy, MAX_ENERGY));
    }
}

/** {@link Creature} with lifecycle */

abstract class Animal implements Creature {

    protected final float weight;
    protected final float speed;
    protected float energy;

    /**
     * Constructs animal from given params.
     * Assumes that all bounds checking was done before
     * @param weight animal's weight
     * @param speed animal's speed
     * @param energy animal's energy
     */

    protected Animal(final float weight, final float speed, final float energy) {
        this.weight = weight;
        this.speed = speed;
        this.energy = energy;
    }

    @Override
    public final float getWeight() {
        return weight;
    }

    @Override
    public final float getSpeed() {
        return speed;
    }

    @Override
    public final float getEnergy() {
        return energy;
    }

    /**
     * Sets energy as current energy + given energy
     * if not greater than {@link #MAX_ENERGY}, otherwise, sets as {@link #MAX_ENERGY}
     * @param energy energy to add
     */

    @Override
    public final void setEnergy(final float energy) {
        this.energy = energy;
    }

    /**
     * Runs all actions that animal should do
     * at the beginning of a new day
     * @param field field that will be eaten by {@link Herbivore}
     * @throws HuntException if there are some errors during the hunt
     */

    abstract void onDayStarted(final Field field, final List<Animal> animals, final int nextInd) throws HuntException;

    /** Gets unique animal's sound */

    abstract String getSound();

    /** Prints animal's sound to console */

    final void makeSound() {
        System.out.println(getSound());
    }

    /**
     * Decrements energy
     * @return if energy <= 0 (animal's dead)
     */

    final boolean onDayEnded() {
        return --energy <= NO_ENERGY;
    }
}

/**
 * State Machine that handles all
 * animals' lifecycles and field's lifecycle
 */

final class ZooStateMachine {
    private final Field field;
    final List<Animal> animals;

    ZooStateMachine(final Field field, final List<Animal> animals) {
        this.field = field;
        this.animals = animals;
    }

    /** Launches all animal's lifecycles one after another */

    private void onDayStarted() {
        for (int i = 0; i < animals.size(); ++i) {
            final Animal animal = animals.get(i);

            try {
                animal.onDayStarted(field, animals, i + 1 == animals.size() ? 0 : (i + 1));
            } catch (final HuntException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Removes all animals with no energy
     * and start grass growing
     */

    private void onDayEnded() {
        animals.removeIf(Animal::onDayEnded);
        field.grassGrow();
    }

    /**
     * Launches the whole day's lifecycle
     * @see #onDayStarted()
     * @see #onDayEnded()
     */

    void handleDay() {
        onDayStarted();
        onDayEnded();
    }

    /**
     * Handles multiple days one after another
     * @param numberOfDays amount of days to handle
     */

    void handleDays(final int numberOfDays) {
        for (int i = 0; i < numberOfDays; ++i) {
            handleDay();
        }
    }

    /** All animals makes sound at the end */

    void makeSounds() {
        animals.forEach(Animal::makeSound);
    }
}

/** {@link Creature} that eats grass */

interface Herbivore extends Creature {
    float EATING_RATIO = 10.0F;

    /** Gets portion equal to weight / {@link #EATING_RATIO} */

    default float calcGrassPortion() {
        return getWeight() / EATING_RATIO;
    }

    /**
     * Checks if grass amount is greater that given portion
     * @param grassAmount total grass amount on {@link Field}
     * @param grassPortion grass portion to eat
     * @see #calcGrassPortion()
     */

    default boolean canEatGrass(final float grassAmount, final float grassPortion) {
        return grassAmount > grassPortion;
    }

    /**
     * Increases energy by given portion
     * @param grassPortion grass to eat
     * @see #addEnergy(float)
     * @see #calcGrassPortion()
     */

    default void eatGrass(final float grassPortion) {
        addEnergy(grassPortion);
    }

    /**
     * Checks if grass is enough to eat by this animal.
     * Then decreases grass amount and increases animal's energy
     * @param field {@link Field} that provides grass
     */

    default void startEatingGrass(final Field field) {
        final float grassPortion = calcGrassPortion();

        if (canEatGrass(field.getGrassAmount(), grassPortion)) {
            field.decreaseGrassAmount(grassPortion);
            eatGrass(grassPortion);
        }
    }
}

/** {@link Creature} that eats other animals */

interface Carnivore extends Creature {

    /**
     * Increases energy by prey's weight
     * @param preyWeight prey's weight
     * @see #addEnergy(float)
     */

    default void setEnergyAfterEating(final float preyWeight) {
        addEnergy(preyWeight);
    }

    /**
     * Tries to eat next animal in the list.
     * If it's possible, next animal will be removed from list
     * @param animals list of all alive animals
     * @param nextInd index of animal to hunt
     */

    default void startHunting(final List<Animal> animals, final int nextInd) throws HuntException {
        final Animal animal = animals.get(nextInd);

        // Checking if there are no animals to hunt
        if (this == animal) {
            throw new SelfHuntingException();
        }

        // Checking if it isn't cannibalism
        if (getClass().isInstance(animal)) {
            throw new CannibalismException();
        }

        // Checking if prey can be hunted
        if (getSpeed() <= animal.speed && getEnergy() <= animal.energy) {
            throw new TooStrongPreyException();
        }

        // Hunt is successful: increasing energy
        // and removing prey from the list

        eatAnimal(animal);
        animals.remove(nextInd);
    }

    /**
     * Increases energy by animal's weight
     * @param animal animal which weight is to add
     * @see #setEnergyAfterEating(float)
     */

    default void eatAnimal(final Animal animal) {
        setEnergyAfterEating(animal.weight);
    }
}

/** {@link Creature} that is both {@link Herbivore} and {@link Carnivore} */

interface Omnivore extends Herbivore, Carnivore {}

/** Lion which is {@link Carnivore} {@link Animal} */

final class Lion extends Animal implements Carnivore {

    Lion(final float weight, final float speed, final float energy) {
        super(weight, speed, energy);
    }

    /**
     * During the day lions only hunt next animals
     * @see #startHunting(List, int)
     */

    @Override
    void onDayStarted(final Field field, final List<Animal> animals, final int nextInd) throws HuntException {
        startHunting(animals, nextInd);
    }

    /** Roooaaar */

    @Override
    String getSound() {
        return "Roar";
    }
}

/** Zebra which is {@link Herbivore} {@link Animal} */

final class Zebra extends Animal implements Herbivore {

    Zebra(final float weight, final float speed, final float energy) {
        super(weight, speed, energy);
    }

    /**
     * During the day zebras only eat grass
     * @see #startEatingGrass(Field)
     */

    @Override
    void onDayStarted(final Field field, final List<Animal> animals, final int nextInd) {
        if (field.getGrassAmount() > Field.NO_GRASS) {
            startEatingGrass(field);
        }
    }

    /** Have you ever wondered what zebra say? */

    @Override
    String getSound() {
        return "Ihoho";
    }
}

/**
 * Boar which is a merciless pig that eats lions and grass.
 * Innopolis's nature is amazing!
 */

final class Boar extends Animal implements Omnivore {

    Boar(final float weight, final float speed, final float energy) {
        super(weight, speed, energy);
    }

    /**
     * During the day boars only eat grass and then lions
     * @see #startEatingGrass(Field)
     * @see #startHunting(List, int)
     */

    @Override
    void onDayStarted(final Field field, final List<Animal> animals, final int nextInd) throws HuntException {
        if (field.getGrassAmount() > Field.NO_GRASS) {
            startEatingGrass(field);
        }

        startHunting(animals, nextInd);
    }

    /** Oink. */

    @Override
    String getSound() {
        return "Oink";
    }
}

/** Warnings during the hunt. Stop current hunt, but not the whole simulation */

abstract class HuntException extends Exception {}

/** Self-hunting is not allowed */

final class SelfHuntingException extends HuntException {
    @Override
    public String getMessage() {
        return "Self-hunting is not allowed";
    }
}

/** Cannibalism is not allowed */

final class CannibalismException extends HuntException {
    @Override
    public String getMessage() {
        return "Cannibalism is not allowed";
    }
}

/** The prey is too strong or too fast to attack */

final class TooStrongPreyException extends HuntException {
    @Override
    public String getMessage() {
        return "The prey is too strong or too fast to attack";
    }
}

/** Illegal argument exceptions that stops the whole program */

abstract class InvalidDataException extends Exception {}

final class WeightOutOfBoundsException extends InvalidDataException {
    @Override
    public String getMessage() {
        return "The weight is out of bounds";
    }
}

/** The energy is out of bounds */

final class EnergyOutOfBoundsException extends InvalidDataException {
    @Override
    public String getMessage() {
        return "The energy is out of bounds";
    }
}

/** The speed is out of bounds */

final class SpeedOutOfBoundsException extends InvalidDataException {
    @Override
    public String getMessage() {
        return "The speed is out of bounds";
    }
}

/** The grass is out of bounds */

final class GrassOutOfBoundsException extends InvalidDataException {
    @Override
    public String getMessage() {
        return "The grass is out of bounds";
    }
}

/** Invalid number of animal parameters */

final class InvalidNumberOfAnimalParametersException extends InvalidDataException {
    @Override
    public String getMessage() {
        return "Invalid number of animal parameters";
    }
}

/** Invalid inputs */

final class InvalidInputsException extends InvalidDataException {
    static final String MESSAGE = "Invalid inputs";

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
