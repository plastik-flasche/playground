import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day4 {
	public static void main(String[] args) {
		String pathToData = Paths.get("AOC2023", "04", "DATA.txt").toAbsolutePath().normalize().toString();

		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(pathToData))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Game game = new Game(lines.toArray(new String[0]));

		System.out.println("Task 1: " + game.getTask1Score());

		System.out.println("Task 2: " + game.getCardNumber());
	}

	static class Game {
		List<CardSet> cards;

		public Game(String[] lines) {
			cards = new ArrayList<>();

			for (String line : lines) {
				cards.add(new CardSet(new Card(line)));
			}

			for (int i = 0; i < cards.size(); i++) {
				CardSet card = cards.get(i);
				int winningNumber = card.getOverlapNumber();
				for (int j = i + 1; j < i + 1 + winningNumber; j++) {
					cards.get(j).increment(card.getAmount());
				}
			}
		}

		public int getTask1Score() {
			return cards.stream().mapToInt(set -> (int) Math.pow(2, set.getOverlapNumber() - 1)).sum();
		}

		public int getCardNumber() {
			return cards.stream().mapToInt(set -> set.getAmount()).sum();
		}

		static class CardSet {
			Card card;
			int amount;

			public int getAmount() {
				return amount;
			}

			public int getOverlapNumber() {
				return card.getOverlapNumber();
			}

			public void increment(int amount) {
				this.amount += amount;
			}

			public CardSet(Card card) {
				this.card = card;
				this.amount = 1;
			}
		}
	}

	static class Card {
		int index;
		List<Integer> winningNumbers;
		List<Integer> yourNumbers;

		public Card(String line) {
			winningNumbers = new ArrayList<>();
			yourNumbers = new ArrayList<>();

			String[] parts = line.split("Card\\s+")[1].split(":\\s+");
			this.index = Integer.parseInt(parts[0]);

			String[] numbers = parts[1].split(" \\|\\s+"); // split into the two halves

			String[] winningStrings = numbers[0].split("\\s+");
			String[] yourStrings = numbers[1].split("\\s+");

			for (String winningString : winningStrings) {
				winningNumbers.add(Integer.parseInt(winningString));
			}

			for (String yourString : yourStrings) {
				yourNumbers.add(Integer.parseInt(yourString));
			}
		}

		public List<Integer> getOverlap() {
			Set<Integer> overlap = new HashSet<>(yourNumbers);
			overlap.retainAll(winningNumbers);
			return new ArrayList<>(overlap);
		}

		public int getOverlapNumber() {
			return getOverlap().size();
		}
	}
}
