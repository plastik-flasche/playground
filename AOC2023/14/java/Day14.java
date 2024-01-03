import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum RockType {
	STATIC_ROCK('#'),
	DYNAMIC_ROCK('O'),
	EMPTY('.');

	private char c;

	RockType(char c) {
		this.c = c;
	}

	static RockType from(int c) {
		return Arrays.stream(values()).filter(t -> t.c == c).findFirst().orElseThrow();
	}

	@Override
	public String toString() {
		return String.valueOf(c);
	}
}

enum Direction {
	NORTH(0, -1),
	WEST(-1, 0),
	SOUTH(0, 1),
	EAST(1, 0);

	private int x;
	private int y;

	Direction(int x, int y) {
		this.x = x;
		this.y = y;

		if (x == 0 && y == 0) {
			throw new RuntimeException("Invalid direction: " + this);
		}

		if (x != 0 && y != 0) {
			throw new RuntimeException("Invalid direction: " + this);
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}

record Grid(ArrayList<ArrayList<RockType>> tiles) {
	public static Grid from(List<String> lines) {
		return new Grid(lines.stream()
				.map(l -> l.chars().mapToObj(RockType::from).collect(Collectors.toCollection(ArrayList::new)))
				.collect(Collectors.toCollection(ArrayList::new)));
	}

	public int getHeight() {
		return tiles.size();
	}

	public int getWidth() {
		return tiles.get(0).size();
	}

	public RockType getRockType(int x, int y) {
		return tiles.get(y).get(x);
	}

	public ArrayList<RockType> getRow(int y) {
		return tiles.get(y);
	}

	public ArrayList<RockType> getColumn(int x) {
		return tiles.stream().map(l -> l.get(x)).collect(Collectors.toCollection(ArrayList::new));
	}

	public void tilt(Direction dir) {
		for (int y = 0; y < getWidth(); y++) {
			for (int x = 0; x < getHeight(); x++) {
				RockType rockType = getRockType(x, y);
				if (rockType == RockType.DYNAMIC_ROCK) {
					int toCheckX = x + dir.getX();
					int toCheckY = y + dir.getY();
					toCheckX = capValue(toCheckX, 0, getWidth() - 1);
					toCheckY = capValue(toCheckY, 0, getHeight() - 1);
					if (getRockType(toCheckX, toCheckY) == RockType.EMPTY) {
						tiles.get(toCheckY).set(toCheckX, RockType.DYNAMIC_ROCK);
						tiles.get(y).set(x, RockType.EMPTY);
						y = capValue(y - 1, 0, getHeight() - 1);
						x--;
					}
				}
			}
		}
	}

	public void cycle() {
		for (Direction dir : Direction.values()) {
			tilt(dir);
		}
	}

	public void cycle(long count) {
		// there's probably a better **MATHEMATICAL** way to do this, but this is
		// sufficient for this data set
		List<Integer> lastHashes = new ArrayList<>();
		lastHashes.add(hashCode());
		long left = -1;
		for (long i = 0; i < count; i++) {
			cycle();
			if (lastHashes.contains(hashCode())) {
				int index = lastHashes.indexOf(hashCode());
				int loopSize = lastHashes.size() - index;
				left = ((count - (i + 1)) % loopSize);
				break;
			}
			lastHashes.add(hashCode());
		}
		for (long i = 0; i < left; i++) {
			cycle();
		}
	}

	private static int capValue(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public long getScore() {
		long score = 0;
		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				RockType rockType = getRockType(x, y);
				if (rockType == RockType.DYNAMIC_ROCK) {
					score += getHeight() - y;
				}
			}
		}
		return score;
	}

	@Override
	public String toString() {
		return tiles.stream().map(l -> l.stream().map(RockType::toString).collect(Collectors.joining()))
				.collect(Collectors.joining("\n"));
	}
}

public class Day14 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "14", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Grid grid = Grid.from(lines);

		grid.tilt(Direction.NORTH);

		System.out.println("Task 1: " + grid.getScore());

		// WARNING: doing this without resetting is only possible because the first
		// value of Direction is also NORTH
		grid.cycle(1000000000);

		System.out.println("Task 2: " + grid.getScore());
	}
}
