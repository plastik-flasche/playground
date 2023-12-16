import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.HashMap;

class Main {
	public static void main(String args[]) {
		// Example:
		// Game 1: 4 red, 5 blue, 9 green; 7 green, 7 blue, 3 red; 16 red, 7 blue, 3
		// green; 11 green, 11 blue, 6 red; 12 red, 14 blue

		String pathToData = Paths.get("AOC2023", "02", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToData))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Game> games = lines.stream().map(line -> new Game(line)).toList();

		final Map<String, Integer> available = Map.of(
				"red", 12,
				"green", 13,
				"blue", 14);

		int task1Sum = games.stream().filter(game -> game.isPossible(available)).mapToInt(game -> game.id).sum();
		System.out.println("Task 1: " + task1Sum);

		int task2Sum = games.stream().mapToInt(game -> game.getPower()).sum();
		System.out.println("Task 2: " + task2Sum);
	}

	static class Game {
		List<SubGame> subGames;
		Map<String, Integer> maxColorAppearing;
		int id;

		public Game(String line) {
			String[] parts = line.split("Game ")[1].split(": ");
			this.id = Integer.parseInt(parts[0]);

			subGames = Arrays.stream(parts[1].split("; ")).map(SubGame::new).toList();

			maxColorAppearing = new HashMap<>();
			for (SubGame subGame : subGames) {
				subGame.colorAppearing.forEach((color, count) -> maxColorAppearing.merge(color, count, Math::max));
			}
		}

		public boolean isPossible(Map<String, Integer> available) {
			return maxColorAppearing.entrySet().stream()
					.allMatch(entry -> available.getOrDefault(entry.getKey(), 0) >= entry.getValue());
		}

		public int getPower() {
			return maxColorAppearing.values().stream().reduce(1, (a, b) -> (a * b));
		}
	}

	static class SubGame {
		Map<String, Integer> colorAppearing;

		public SubGame(String gameString) {
			colorAppearing = new HashMap<String, Integer>();
			String[] colors = gameString.split(", ");
			for (String color : colors) {
				String[] colorAndCount = color.split(" ");
				String name = colorAndCount[1];
				int count = Integer.parseInt(colorAndCount[0]);
				colorAppearing.put(name, count);
			}
		}
	}
}
