import random

# Define board size
BOARD_SIZE = 5  # You can adjust this to be larger or smaller


# Empty board template
def create_empty_board(size):
    return [['~' for _ in range(size)] for _ in range(size)]


# Initialize boards for two players
player1_board = create_empty_board(BOARD_SIZE)
player2_board = create_empty_board(BOARD_SIZE)

# Example ship placements (1 represents a ship; 0 represents water)
# Adjust the matrix as per your desired ship placements
player1_ships = [
    [1, 1, 1, 1, 1],
    [0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0]
]

player2_ships = [
    [1, 0, 0, 0, 0],
    [1, 0, 0, 0, 0],
    [1, 0, 0, 0, 0],
    [1, 0, 0, 0, 0],
    [1, 0, 0, 0, 0]
]


# Function to display the board
def print_board(board, hide_ships=True):
    for row in board:
        print(" ".join('O' if cell == 'S' and hide_ships else cell for cell in row))
    print()


# Function to take a shot
def take_shot(board, ship_matrix, row, col):
    if ship_matrix[row][col] == 1:
        board[row][col] = 'X'  # Hit
        print("It's a hit!")
        return True
    else:
        board[row][col] = 'M'  # Miss
        print("It's a miss.")
        return False


# Game loop
def play_battleship():
    player_turn = 1
    moves = 0

    while True:
        print(f"\nPlayer {player_turn}'s turn.")

        # Show boards
        print("\nPlayer 1 Board:")
        print_board(player1_board)
        print("\nPlayer 2 Board:")
        print_board(player2_board)

        # Get shot coordinates
        try:
            row, col = map(int, input("Enter row and column (e.g., '2 3'): ").split())
        except ValueError:
            print("Invalid input. Try again.")
            continue

        # Check if shot is in bounds
        if not (0 <= row < BOARD_SIZE and 0 <= col < BOARD_SIZE):
            print("Coordinates out of bounds. Try again.")
            continue

        # Handle player turns and update boards
        if player_turn == 1:
            if take_shot(player2_board, player2_ships, row, col):
                print("Player 1 scored a hit!")
            player_turn = 2
        else:
            if take_shot(player1_board, player1_ships, row, col):
                print("Player 2 scored a hit!")
            player_turn = 1

        moves += 1
        # Optional: Set a winning condition based on all ships being sunk or moves count

        if moves >= 20:  # Just an example limit
            print("Game Over")
            break


play_battleship()
