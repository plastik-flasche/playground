import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

enum Type {
	EMPTY('.', Map.of()),
	VERTICAL('|', Map.of(
			MirrorDirection.EAST, Set.of(MirrorDirection.NORTH, MirrorDirection.SOUTH),
			MirrorDirection.WEST, Set.of(MirrorDirection.NORTH, MirrorDirection.SOUTH))),
	HORIZONTAL('-', Map.of(
			MirrorDirection.NORTH, Set.of(MirrorDirection.EAST, MirrorDirection.WEST),
			MirrorDirection.SOUTH, Set.of(MirrorDirection.EAST, MirrorDirection.WEST))),
	FORWARD_DIAG('/', Map.of(
			MirrorDirection.NORTH, Set.of(MirrorDirection.EAST),
			MirrorDirection.EAST, Set.of(MirrorDirection.NORTH),
			MirrorDirection.SOUTH, Set.of(MirrorDirection.WEST),
			MirrorDirection.WEST, Set.of(MirrorDirection.SOUTH))),
	BACKWARD_DIAG('\\', Map.of(
			MirrorDirection.NORTH, Set.of(MirrorDirection.WEST),
			MirrorDirection.WEST, Set.of(MirrorDirection.NORTH),
			MirrorDirection.SOUTH, Set.of(MirrorDirection.EAST),
			MirrorDirection.EAST, Set.of(MirrorDirection.SOUTH)));

	private char c;
	private Map<MirrorDirection, Set<MirrorDirection>> mirrorsBehavior;

	Type(char c, Map<MirrorDirection, Set<MirrorDirection>> map) {
		this.c = c;
		mirrorsBehavior = new HashMap<>(map);
		for (MirrorDirection d : MirrorDirection.values()) {
			if (!mirrorsBehavior.containsKey(d)) {
				mirrorsBehavior.put(d, Set.of(d));
			}
		}
	}

	public static Type fromChar(int c) {
		return Arrays.stream(values()).filter(t -> t.c == c).findFirst().orElseThrow();
	}

	public Set<MirrorDirection> getMirrorsOutputs(MirrorDirection d) {
		return mirrorsBehavior.get(d);
	}

	@Override
	public String toString() {
		return String.valueOf(c);
	}
}

enum MirrorDirection {
	NORTH(0, -1),
	WEST(-1, 0),
	SOUTH(0, 1),
	EAST(1, 0);

	private int x;
	private int y;

	MirrorDirection(int x, int y) {
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

class MirrorTile {
	private Type type;
	private Set<MirrorDirection> alreadyTraversed = new HashSet<>();

	public boolean isTraversed() {
		return !alreadyTraversed.isEmpty();
	}

	public Set<MirrorDirection> traverse(MirrorDirection d) {
		Set<MirrorDirection> outputs = new HashSet<>(type.getMirrorsOutputs(d));
		outputs.removeAll(alreadyTraversed);
		alreadyTraversed.addAll(outputs);
		return outputs;
	}

	public MirrorTile(Type type) {
		this.type = type;
	}
}

class Beam {
	int x; // coord of next tile, so if coming from 0,0 and heading east, x = 1 and y = 0
	int y;
	MirrorDirection direction;

	public Beam(int x, int y, MirrorDirection direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
}

class MirrorGrid {
	private MirrorTile[][] tiles;
	List<Beam> beams = new ArrayList<>();

	public MirrorGrid(List<String> lines) {
		tiles = new MirrorTile[lines.get(0).length()][lines.size()];
		for (int y = 0; y < lines.size(); y++) {
			for (int x = 0; x < lines.get(y).length(); x++) {
				tiles[x][y] = new MirrorTile(Type.fromChar(lines.get(y).charAt(x)));
			}
		}

		beams = new ArrayList<>();
		beams.add(new Beam(0, 0, MirrorDirection.EAST));
	}

	public long getTraversedCount() {
		while (!beams.isEmpty()) {
			Beam beam = beams.remove(0);
			Set<MirrorDirection> mirrorsOutputs = tiles[beam.x][beam.y].traverse(beam.direction);
			for (MirrorDirection d : mirrorsOutputs) {
				int newX = beam.x + d.getX();
				int newY = beam.y + d.getY();
				if (newX < 0 || newX >= tiles.length || newY < 0 || newY >= tiles[0].length) {
					continue;
				}
				beams.add(new Beam(beam.x + d.getX(), beam.y + d.getY(), d));
			}
		}
		return Arrays.stream(tiles).flatMap(Arrays::stream).filter(MirrorTile::isTraversed).count();
	}
}

public class Day16 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "16", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		MirrorGrid grid = new MirrorGrid(lines);
		long task1 = grid.getTraversedCount();
		System.out.println("Task 1: " + task1);
	}
}
