import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageOps

# Operation: Cyan to Magenta Gradient
START_COLOR = (0, 229, 255) # Cyan #00E5FF
END_COLOR = (255, 0, 255)   # Magenta #FF00FF

def interpolate(color1, color2, t):
    return tuple(int(a + (b - a) * t) for a, b in zip(color1, color2))

def apply_gradient(image_path):
    print(f"Processing: {image_path}")
    try:
        img = Image.open(image_path).convert("RGBA")
        width, height = img.size
        
        # Create gradient
        gradient = Image.new('RGBA', (width, height), (0,0,0,0))
        draw = ImageDraw.Draw(gradient)
        
        for y in range(height):
            t = y / height
            color = interpolate(START_COLOR, END_COLOR, t)
            draw.line([(0, y), (width, y)], fill=color + (255,))
            
        # Composite: Gradient masked by original alpha
        r, g, b, alpha = img.split()
        gradient.putalpha(alpha)
        
        gradient.save(image_path, "PNG")
        print(f"  -> Applied gradient to {image_path}")
        
    except Exception as e:
        print(f"  -> Error: {e}")

def main():
    base_dir = Path(r"c:\Users\ktrou\AndroidStudioProjects\PersonalLevelingSystem\app\src\main\res")
    
    # Process all mipmap folders
    for folder in base_dir.glob("mipmap-*"):
        if folder.is_dir():
            icon_path = folder / "ic_launcher_foreground.png"
            if icon_path.exists():
                apply_gradient(icon_path)
            
            # Also try round foregrounds if they exist distinct from rectangular
            # Usually foreground is shared, but let's check
            
if __name__ == "__main__":
    main()
