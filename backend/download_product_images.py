#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量下载商品图片脚本
根据商品名称从网络下载适配的图片
"""
import os
import requests
import hashlib
import time
from datetime import datetime

# 商品列表（ID和名称）
products = [
    (2, "三只松鼠 碧根果 500g"),
    (3, "百草味 猪肉脯 200g"),
    (4, "卫龙 大面筋 106g*10包"),
    (5, "洽洽 瓜子 308g 焦糖味"),
    (6, "乐事 薯片 大波浪 原味 135g"),
    (7, "旺旺 仙贝 540g 家庭装"),
    (9, "农夫山泉 东方树叶 茉莉花茶 500ml*15"),
    (10, "元气森林 白桃气泡水 480ml*12"),
    (11, "三得利 乌龙茶 500ml*18"),
    (12, "雀巢 速溶咖啡 1+2 微研磨 100条"),
    (13, "星巴克 即饮咖啡 抹茶拿铁 270ml*6"),
    (14, "康师傅 冰红茶 500ml*15"),
    (15, "立顿 红茶包 100包装"),
    (16, "金龙鱼 调和油 5L"),
    (17, "海天 生抽酱油 1.9L"),
    (18, "太太乐 鸡精 454g"),
    (19, "恒顺 镇江香醋 580ml"),
    (20, "李锦记 蚝油 510g"),
    (21, "福临门 东北大米 10kg"),
    (22, "十三香 调味料 45g"),
    (23, "康师傅 红烧牛肉面 5连包"),
    (24, "统一 老坛酸菜牛肉面 5连包"),
    (25, "自嗨锅 麻辣火锅 自热锅"),
    (26, "日清 合味道 杯面 海鲜味 12杯"),
    (28, "阿宽 红油面皮 4连包"),
    (29, "海底捞 自煮火锅 蔬菜版"),
    (30, "蒙牛 特仑苏 纯牛奶 250ml*12"),
    (31, "伊利 安慕希 原味酸奶 205g*12"),
    (32, "光明 莫斯利安 原味酸奶 200g*12"),
    (33, "德亚 全脂纯牛奶 200ml*24 德国进口"),
    (34, "认养一头牛 全脂纯牛奶 250ml*12"),
    (35, "奥利奥 夹心饼干 原味 388g"),
    (36, "达利园 蛋黄派 600g"),
    (37, "Pocky 百奇 巧克力味 55g*10"),
    (38, "稻香村 糕点礼盒 1500g"),
    (39, "好丽友 派 巧克力味 12枚"),
    (40, "嘉士利 早餐饼干 800g"),
    (41, "费列罗 榛果威化巧克力 30粒"),
    (42, "德芙 丝滑牛奶巧克力 252g"),
    (43, "徐福记 沙琪玛 470g"),
    (44, "明治 巧克力 特纯黑巧 75%可可"),
    (45, "不二家 棒棒糖 混合口味 40支"),
    (46, "阿尔卑斯 太妃糖 200g"),
    (47, "韩国 农心 辛拉面 5连包"),
    (48, "日本 格力高 百醇 注心饼干 48g*6"),
    (49, "泰国 皇家金枕 榴莲干 300g"),
    (50, "越南 G7 三合一速溶咖啡 50条"),
    (51, "澳洲 TimTam 巧克力饼干 200g"),
    (52, "意大利 Barilla 百味来 意面 500g"),
    (53, "来伊份 芒果干 120g"),
    (54, "君乐宝 简醇 0添加蔗糖酸奶 250g*10"),
    (55, "白象 大骨面 香辣牛骨味 5连包"),
    (56, "桃李 醇熟切片吐司 400g"),
    (57, "231253"),
]

# 图片URL映射（使用免费的高质量商品图片资源）
# 由于无法实时搜索，这里使用一些预定义的商品图片URL
# 实际应用中可以调用图片搜索API
IMAGE_URLS = {
    # 坚果类
    2: "https://images.unsplash.com/photo-1596662951482-0c4ba74a6df6?w=400&h=400&fit=crop",  # 碧根果
    3: "https://images.unsplash.com/photo-1625944525533-473f1a3d54e7?w=400&h=400&fit=crop",  # 猪肉脯
    4: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 面筋/面条
    5: "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=400&h=400&fit=crop",  # 瓜子/坚果
    # 薯片零食
    6: "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400&h=400&fit=crop",  # 薯片
    7: "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?w=400&h=400&fit=crop",  # 仙贝/饼干
    # 饮料
    9: "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop",  # 茶饮料
    10: "https://images.unsplash.com/photo-1563822249366-3efb23b8e0c9?w=400&h=400&fit=crop",  # 气泡水
    11: "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&h=400&fit=crop",  # 乌龙茶
    12: "https://images.unsplash.com/photo-1610889556528-9a770e32642f?w=400&h=400&fit=crop",  # 咖啡
    13: "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400&h=400&fit=crop",  # 拿铁
    14: "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop",  # 冰红茶
    15: "https://images.unsplash.com/photo-1563822249366-3efb23b8e0c9?w=400&h=400&fit=crop",  # 茶包
    # 调料
    16: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&h=400&fit=crop",  # 食用油
    17: "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop",  # 酱油
    18: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=400&fit=crop",  # 调料
    19: "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop",  # 醋
    20: "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop",  # 蚝油
    21: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=400&fit=crop",  # 大米
    22: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=400&fit=crop",  # 调味料
    # 方便面
    23: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 方便面
    24: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 方便面
    25: "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=400&fit=crop",  # 火锅
    26: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 杯面
    28: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 面皮
    29: "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=400&fit=crop",  # 火锅
    # 乳制品
    30: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop",  # 牛奶
    31: "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop",  # 酸奶
    32: "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop",  # 酸奶
    33: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop",  # 牛奶
    34: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop",  # 牛奶
    # 饼干糕点
    35: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 饼干
    36: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 蛋黄派
    37: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 百奇
    38: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=400&fit=crop",  # 糕点
    39: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 派
    40: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 饼干
    # 巧克力糖果
    41: "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400&h=400&fit=crop",  # 巧克力
    42: "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400&h=400&fit=crop",  # 巧克力
    43: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 沙琪玛
    44: "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400&h=400&fit=crop",  # 巧克力
    45: "https://images.unsplash.com/photo-1589386948082-414a873d328f?w=400&h=400&fit=crop",  # 棒棒糖
    46: "https://images.unsplash.com/photo-1589386948082-414a873d328f?w=400&h=400&fit=crop",  # 糖果
    # 进口食品
    47: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 辛拉面
    48: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 饼干
    49: "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400&h=400&fit=crop",  # 榴莲干
    50: "https://images.unsplash.com/photo-1610889556528-9a770e32642f?w=400&h=400&fit=crop",  # 咖啡
    51: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop",  # 饼干
    52: "https://images.unsplash.com/photo-1551183053-bf91b1d31191?w=400&h=400&fit=crop",  # 意面
    53: "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400&h=400&fit=crop",  # 芒果干
    54: "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop",  # 酸奶
    55: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 方便面
    56: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=400&fit=crop",  # 吐司
    57: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop",  # 默认
}

# 保存目录
SAVE_DIR = "d:/Projects/Windsurf/GoodsManage/backend/uploads/2026/05/19"

def generate_filename(product_id, url):
    """生成唯一的文件名"""
    # 使用商品ID和URL的哈希值生成文件名
    hash_input = f"{product_id}_{url}"
    hash_obj = hashlib.md5(hash_input.encode())
    filename = f"{hash_obj.hexdigest()}.jpg"
    return filename

def download_image(url, save_path):
    """下载图片"""
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        with open(save_path, 'wb') as f:
            f.write(response.content)
        
        print(f"✓ 下载成功: {save_path}")
        return True
    except Exception as e:
        print(f"✗ 下载失败: {url} - {str(e)}")
        return False

def main():
    """主函数"""
    print(f"开始下载商品图片...")
    print(f"保存目录: {SAVE_DIR}")
    print(f"商品数量: {len(products)}")
    print("-" * 60)
    
    success_count = 0
    fail_count = 0
    results = []
    
    for product_id, product_name in products:
        if product_id not in IMAGE_URLS:
            print(f"⚠ 跳过 {product_id}: {product_name} (无对应图片URL)")
            continue
        
        url = IMAGE_URLS[product_id]
        filename = generate_filename(product_id, url)
        save_path = os.path.join(SAVE_DIR, filename)
        
        # 如果文件已存在，跳过
        if os.path.exists(save_path):
            print(f"⊘ 文件已存在: {product_id}: {product_name}")
            results.append((product_id, filename))
            success_count += 1
            continue
        
        # 下载图片
        if download_image(url, save_path):
            results.append((product_id, filename))
            success_count += 1
        else:
            fail_count += 1
        
        # 避免请求过于频繁
        time.sleep(0.5)
    
    print("-" * 60)
    print(f"下载完成！成功: {success_count}, 失败: {fail_count}")
    
    # 生成SQL更新语句
    print("\n生成SQL更新语句:")
    print("-" * 60)
    for product_id, filename in results:
        image_path = f"/uploads/2026/05/19/{filename}"
        sql = f"UPDATE product SET images = '{image_path}' WHERE id = {product_id};"
        print(sql)
    
    # 保存SQL到文件
    sql_file = "d:/Projects/Windsurf/GoodsManage/backend/update_product_images.sql"
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write("-- 批量更新商品图片路径\n")
        f.write("-- 生成时间: {}\n\n".format(datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
        for product_id, filename in results:
            image_path = f"/uploads/2026/05/19/{filename}"
            f.write(f"UPDATE product SET images = '{image_path}' WHERE id = {product_id};\n")
    
    print(f"\nSQL语句已保存到: {sql_file}")

if __name__ == "__main__":
    main()
