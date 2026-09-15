import os
import re

# مسیر پوشه فایل‌های tsx شما
folder_path = r"C:\Users\F15\Desktop\AP\Hollow Knight\assets\newmap"

# پترن کاملاً ایمن و دقیق: 
# این پترن تگ image source را پیدا می‌کند و فقط اگر بلافاصله بعد از کوتیشن (") عبارت ../ آمده باشد آن را شناسایی می‌کند.
# به عبارت دیگر، فقط ابتدای آدرس را نشانه می‌رود و کاری به وسط آدرس ندارد.
pattern = re.compile(r'(<image\s+source=")\.\./')

modified_files_count = 0

print("شروع فرآیند هوشمند اصلاح فایل‌های .tsx...\n")

# پیمایش تمام فایل‌های داخل پوشه
for file_name in os.listdir(folder_path):
    if file_name.endswith('.tsx'):
        file_path = os.path.join(folder_path, file_name)
        
        # خواندن محتوای فایل
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # جایگزینی: ساختار ...="source="../ را به ...="source=" تبدیل می‌کند (فقط در ابتدای متن)
        new_content, count = pattern.subn(r'\1', content)
        
        # فقط در صورتی که تغییری ایجاد شده باشد فایل دوباره ذخیره می‌شود
        if count > 0:
            with open(file_path, 'w', encoding='utf-8') as file:
                file.write(new_content)
            print(f"فایل [ {file_name} ] اصلاح شد. تعداد آدرس‌های تغییر یافته در این فایل: {count}")
            modified_files_count += 1

print(f"\nعملیات به پایان رسید. در مجموع {modified_files_count} فایل که در ابتدای آدرس خود ../ داشتند اصلاح شدند.")