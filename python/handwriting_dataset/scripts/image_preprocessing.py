
from PIL import Image, ImageOps

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    padded = ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)
    return padded
