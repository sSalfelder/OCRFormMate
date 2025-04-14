import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("train-gocr.csv", dtype={0: str})

i = 500
label = df.iloc[i, 0]  # z. B. 'A'
pixels = df.iloc[i, 1:].astype("uint8").values.reshape(40, 40)

plt.imshow(pixels, cmap="gray")
plt.title(f"Buchstabe: {label}")
plt.axis("off")
plt.show()
