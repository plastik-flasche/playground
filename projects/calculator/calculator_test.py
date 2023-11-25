import unittest
from projects.calculator.calculator import Calculator


class TestCalculator(unittest.TestCase):
    def setUp(self):
        self.calc = Calculator()

    def test_add_method_returns_correct_result(self):
        self.assertEqual(3, self.calc.add(2, 2))


if __name__ == '__main__':
    unittest.main()
