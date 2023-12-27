import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Day11 {
	// for more complex paths, like avoiding other galaxies, I could use A*... and
	// when expanding, I could just increase the weights of the edges that would be
	// expanded
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "11", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(Paths.get(pathToData));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Image task1 = Image.parse(lines);
		task1.expand(2);
		System.out.println(task1.getSumOfNonObstructedPaths());

		Image task2 = Image.parse(lines);
		task2.expand(1000000);
		System.out.println(task2.getSumOfNonObstructedPaths());
	}

	static record Position(int x, int y) {
		public Position subtract(Position other) {
			return new Position(x - other.x(), y - other.y());
		}
	}

	static class Image {
		private List<Position> galaxies;

		private Image(List<Position> galaxies) {
			this.galaxies = galaxies;
		}

		public static Image parse(List<String> lines) {
			List<Position> galaxies = new ArrayList<>();

			for (int y = 0; y < lines.size(); y++) {
				String line = lines.get(y);
				for (int x = 0; x < line.length(); x++) {
					if (line.charAt(x) == '#') {
						galaxies.add(new Position(x, y));
					}
				}
			}

			return new Image(galaxies);
		}

		public void expand(int amount) {
			List<Position> sortedByX = galaxies.stream()
					.sorted((a, b) -> Integer.compare(a.x(), b.x()))
					.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

			int lastX = -1;
			int offset = 0;
			for (int i = 0; i < sortedByX.size(); i++) {
				int newX = sortedByX.get(i).x() + offset;
				if (newX - lastX > 1) {
					offset += amount - 1;
					newX += amount - 1;
				}
				sortedByX.set(i, new Position(newX, sortedByX.get(i).y()));
				lastX = newX;
			}

			List<Position> sortedByY = sortedByX.stream()
					.sorted((a, b) -> Integer.compare(a.y(), b.y()))
					.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

			int lastY = -1;
			offset = 0;
			for (int i = 0; i < sortedByY.size(); i++) {
				int newY = sortedByY.get(i).y() + offset;
				if (newY - lastY > 1) {
					offset += amount - 1;
					newY += amount - 1;
				}
				sortedByY.set(i, new Position(sortedByY.get(i).x(), newY));
				lastY = newY;
			}

			galaxies = sortedByY;
		}

		public long getSumOfNonObstructedPaths() {
			long sum = 0;
			for (int i = 0; i < galaxies.size(); i++) {
				for (int j = i + 1; j < galaxies.size(); j++) {
					sum += getPathLengthBetween(galaxies.get(i), galaxies.get(j));
				}
			}
			return sum;
		}

		private long getPathLengthBetween(Position from, Position to) {
			Position delta = to.subtract(from);
			return Math.abs(delta.x) + Math.abs(delta.y);
		}

		@Override
		public String toString() {
			List<List<Character>> grid = new ArrayList<>();
			int maxX = 0;
			for (Position galaxy : galaxies) {
				if (galaxy.x() > maxX) {
					maxX = galaxy.x();
				}
				while (grid.size() <= galaxy.y()) {
					grid.add(new ArrayList<>());
				}
				List<Character> row = grid.get(galaxy.y());
				while (row.size() <= galaxy.x()) {
					row.add('.');
				}
				row.set(galaxy.x(), '#');
			}
			for (List<Character> row : grid) {
				while (row.size() <= maxX) {
					row.add('.');
				}
			}

			StringBuilder sb = new StringBuilder();
			for (List<Character> row : grid) {
				for (Character c : row) {
					sb.append(c);
				}
				sb.append('\n');
			}
			return sb.toString();
		}
	}
}