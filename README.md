# Shamir’s Secret Sharing (SSS) - Secret Reconstruction

This project implements a solution to reconstruct the secret in **Shamir’s Secret Sharing (SSS)** scheme using **Lagrange interpolation**.  
It also detects corrupted (wrong) shares among the inputs.

---

## 📌 Problem Overview
- A secret `c` is hidden in the constant term of a polynomial `F(x)`.
- Each participant receives a share `(x, y)` where `y = F(x)`.
- Any **K valid shares** (threshold) can reconstruct the polynomial and recover the secret `F(0) = c`.
- If corrupted shares are present, the program identifies them by checking consistency among subsets.

---

## 🚀 Features
- Handles big integers (20–40 digits) using Java `BigInteger`.
- Supports shares given in **different bases** (binary, hex, base-7, etc.).
- Works with **JSON input format**:
  ```json
  {
    "keys": { "n": 4, "k": 3 },
    "1": { "base": "10", "value": "4" },
    "2": { "base": "2", "value": "111" },
    "3": { "base": "10", "value": "12" },
    "6": { "base": "4", "value": "213" }
  }
