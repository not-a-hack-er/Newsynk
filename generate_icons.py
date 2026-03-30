import os
import glob
from PIL import Image

downloads_path = os.path.expanduser('~\\Downloads')
images = glob.glob(os.path.join(downloads_path, '*.png')) + glob.glob(os.path.join(downloads_path, '*.jpg'))
if not images:
    print("No images found in Downloads")
    exit(1)

latest_image = max(images, key=os.path.getctime)
print(f"Using {latest_image}")

img = Image.open(latest_image).convert("RGBA")

# The logo is dark. Sample the top-left pixel to get background color
bg_color = img.getpixel((0, 0))

# Make the canvas a square
max_dim = max(img.size)
# Add 30% padding so it fits well inside circular masks for app icons
target_size = int(max_dim * 1.3)
new_img = Image.new("RGBA", (target_size, target_size), bg_color)
offset = ((target_size - img.size[0]) // 2, (target_size - img.size[1]) // 2)
new_img.paste(img, offset, img)

bg_rgb = bg_color[:3]
bg_hex = "%02x%02x%02x" % bg_rgb

res_path = "src/main/res"

if not os.path.exists(res_path):
    print("Run from app directory")
    exit(1)

# Write adaptive vector background
drawable_v24_dir = os.path.join(res_path, "drawable")
os.makedirs(drawable_v24_dir, exist_ok=True)
with open(os.path.join(drawable_v24_dir, "ic_launcher_background.xml"), "w") as f:
    f.write(f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#{bg_hex}"
        android:pathData="M0,0h108v108h-108z"/>
</vector>""")

densities = {
    'mdpi': 1,
    'hdpi': 1.5,
    'xhdpi': 2,
    'xxhdpi': 3,
    'xxxhdpi': 4
}

for density, multiplier in densities.items():
    # Adaptive foreground size is 108dp
    fg_size = int(108 * multiplier)
    # Legacy icon size is 48dp
    legacy_size = int(48 * multiplier)
    
    mipmap_dir = os.path.join(res_path, f"mipmap-{density}")
    os.makedirs(mipmap_dir, exist_ok=True)
    
    # Generate foreground
    fg_img = new_img.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
    fg_img.save(os.path.join(mipmap_dir, "ic_launcher_foreground.png"))
    
    # Generate legacy 
    legacy_img = new_img.resize((legacy_size, legacy_size), Image.Resampling.LANCZOS)
    legacy_img.save(os.path.join(mipmap_dir, "ic_launcher.png"))
    legacy_img.save(os.path.join(mipmap_dir, "ic_launcher_round.png"))

anydpi_dir = os.path.join(res_path, "mipmap-anydpi-v26")
os.makedirs(anydpi_dir, exist_ok=True)

adaptive_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>"""

with open(os.path.join(anydpi_dir, "ic_launcher.xml"), "w") as f:
    f.write(adaptive_xml)
with open(os.path.join(anydpi_dir, "ic_launcher_round.xml"), "w") as f:
    f.write(adaptive_xml)
    
print("Successfully generated all app icons!")
