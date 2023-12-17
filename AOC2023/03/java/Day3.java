import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Day3 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "03", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToData))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Schematic schematic = new Schematic(lines.toArray(new String[0]));

		int task1Sum = schematic.getPartNumbersAdjacentToSymbol().stream().mapToInt(partNumber -> partNumber.number)
				.sum();
		System.out.println("Task 1: " + task1Sum);

		int task2Sum = schematic.getGears().stream().mapToInt(gear -> gear.getRatio()).sum();
		System.out.println("Task 2: " + task2Sum);
	}

	static class PartNumber {
		int number;
		List<Position> digitPositions;

		public PartNumber(int number, Position MSDPosition) {
			this.number = number;
			digitPositions = new ArrayList<>();

			int numberOfDigits = (int) Math.log10(number) + 1;
			for (int i = 0; i < numberOfDigits; i++) {
				digitPositions.add(MSDPosition.copy());
				MSDPosition.x++;
			}
		}

		public boolean isAdjacentTo(List<Position> positions) {
			for (Position position : positions) {
				if (isAdjacentTo(position)) {
					return true;
				}
			}
			return false;
		}

		public boolean isAdjacentTo(Position position) {
			for (Position digitPosition : digitPositions) {
				// is within 1 unit of each other
				if (Math.abs(digitPosition.x - position.x) <= 1 && Math.abs(digitPosition.y - position.y) <= 1) {
					return true;
				}
			}
			return false;
		}
	}

	static class Position {
		int x;
		int y;

		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}

		Position copy() {
			return new Position(x, y);
		}
	}

	static class Gear {
		Position position;

		int ratio = -1;

		public Gear(Position position) {
			this.position = position;
		}

		public int getRatio() {
			return ratio;
		}

		public boolean isValid(List<PartNumber> partNumbers) {
			int[] adjacent = new int[2];

			for (int i = 0; i < adjacent.length; i++) {
				adjacent[i] = -1;
			}

			for (PartNumber partNumber : partNumbers) {
				if (partNumber.isAdjacentTo(position)) {
					boolean added = false;
					for (int i = 0; i < adjacent.length; i++) {
						if (adjacent[i] == -1) {
							adjacent[i] = partNumber.number;
							added = true;
							break;
						}
					}
					if (!added) {
						// Too many neighbors
						return false;
					}
				}
			}

			for (int num : adjacent) {
				if (num == -1) {
					// Too few neighbors
					return false;
				}
			}

			ratio = Arrays.stream(adjacent).reduce(1, (a, b) -> (a * b));

			return true;
		}
	}

	static class Schematic {
		List<PartNumber> partNumbers;
		List<Position> symbolPositions;
		List<Gear> gears;

		public Schematic(String[] lines) {
			partNumbers = new ArrayList<>();
			symbolPositions = new ArrayList<>();

			List<Gear> possibleGears = new ArrayList<>();

			for (int y = 0; y < lines.length; y++) {
				String line = lines[y];
				for (int x = 0; x < line.length(); x++) {
					char c = line.charAt(x);
					if (c == '.') {
						continue;
					} else if (Character.isDigit(c)) {
						int number = 0;
						final Position MSDPosition = new Position(x, y);
						while (x < line.length() && Character.isDigit(line.charAt(x))) {
							number = number * 10 + Character.getNumericValue(line.charAt(x));
							x++;
						}
						x--; // Backtrack one character to account for the for loop increment
						partNumbers.add(new PartNumber(number, MSDPosition));
					} else {
						symbolPositions.add(new Position(x, y));
						if (c == '*') {
							possibleGears.add(new Gear(new Position(x, y)));
						}
					}
				}
			}

			gears = possibleGears.stream().filter(gear -> gear.isValid(partNumbers)).toList();

			System.out.println(possibleGears.size());
			System.out.println(gears.size());
		}

		public List<PartNumber> getPartNumbersAdjacentToSymbol() {
			return partNumbers.stream()
					.filter(partNumber -> partNumber.isAdjacentTo(symbolPositions)).toList();
		}

		public List<Gear> getGears() {
			return gears;
		}
	}
}
