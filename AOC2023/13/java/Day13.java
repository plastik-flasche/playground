import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

enum Tile {
	ROCK('#'),
	ASH('.');

	private char c;

	Tile(char c) {
		this.c = c;
	}

	static Tile from(int c) {
		return Arrays.stream(values()).filter(t -> t.c == c).findFirst().orElseThrow();
	}
}

record Line(List<Tile> tiles) {
	public static Line from(String line) {
		return new Line(line.chars().mapToObj(Tile::from).toList());
	}
}

record Pattern(List<Line> lines) {
	public static Pattern from(String pattern) {
		return new Pattern(pattern.lines().map(Line::from).toList());
	}

	public List<Integer> getHorizontalSymmetriesStart(int nonMatchingCount) {
		return getSymmetriesStart(true, nonMatchingCount);
	}

	public List<Integer> getVerticalSymmetriesStart(int nonMatchingCount) {
		return getSymmetriesStart(false, nonMatchingCount);
	}

	private List<Integer> getSymmetriesStart(boolean isHorizontal, int nonMatchingCount) {
		List<Integer> symmetries = new ArrayList<>();
		int dimension = isHorizontal ? getHeight() : getWidth();

		for (int i = 0; i < dimension - 1; i++) {
			int minDistanceToEdge = Math.min(i, dimension - i - 2);
			int nonMatching = 0;
			for (int j = 0; j < minDistanceToEdge + 1; j++) {
				List<Tile> first = isHorizontal ? getRow(i - j) : getColumn(i - j);
				List<Tile> second = isHorizontal ? getRow(i + j + 1) : getColumn(i + j + 1);
				nonMatching += getNonMatchingCount(first, second);
				if (nonMatching > nonMatchingCount) {
					break;
				}
			}
			if (nonMatching == nonMatchingCount) {
				symmetries.add(i);
			}
		}
		return symmetries;
	}

	private int getNonMatchingCount(List<Tile> first, List<Tile> second) {
		int count = 0;
		for (int i = 0; i < first.size(); i++) {
			if (first.get(i) != second.get(i)) {
				count++;
			}
		}
		return count;
	}

	public List<Tile> getRow(int row) {
		return lines.get(row).tiles();
	}

	public List<Tile> getColumn(int column) {
		return lines.stream().map(l -> l.tiles().get(column)).toList();
	}

	public int getWidth() {
		return lines.get(0).tiles().size();
	}

	public int getHeight() {
		return lines.size();
	}

	public long getAnswer(int nonMatchingCount) {
		long answerHorizontal = getHorizontalSymmetriesStart(nonMatchingCount).stream()
				.mapToLong(number -> number + 1)
				.sum();

		long answerVertical = getVerticalSymmetriesStart(nonMatchingCount).stream()
				.mapToLong(number -> number + 1)
				.sum();

		return answerVertical + answerHorizontal * 100;
	}
}

public class Day13 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "13", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String all = lines.stream().collect(Collectors.joining("\n"));

		String[] patternStrings = all.split("\n\n");

		List<Pattern> patterns = Arrays.stream(patternStrings).map(Pattern::from).toList();

		long task1Answer = patterns.stream().mapToLong(p -> p.getAnswer(0)).sum();

		System.out.println("Task 1: " + task1Answer);

		long task2Answer = patterns.stream().mapToLong(p -> p.getAnswer(1)).sum();

		System.out.println("Task 2: " + task2Answer);
	}
}