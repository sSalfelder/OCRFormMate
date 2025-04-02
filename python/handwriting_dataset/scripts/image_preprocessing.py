# image_preprocessing.py

from PIL import Image, ImageOps

def prepare_image(image: Image.Image, size=(384, 384)) -> Image.Image:
    """
    Pads the image to a square and resizes it to the target size.
    Keeps the aspect ratio and avoids distortion.
    """
    padded = ImageOps.pad(image, size, color="white", centering=(0.5, 0.5), method=Image.BICUBIC)
    return padded
