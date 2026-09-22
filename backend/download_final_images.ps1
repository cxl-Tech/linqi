# 下载最后14个商品图片脚本
# 为剩余的14个商品下载图片

# 保存目录
$SAVE_DIR = "d:/Projects/Windsurf/GoodsManage/backend/uploads/2026/05/19"

# 最后14个商品图片URL映射
$IMAGE_URLS = @{
    4 = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop"  # 面筋
    11 = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&h=400&fit=crop"  # 乌龙茶
    12 = "https://images.unsplash.com/photo-1610889556528-9a770e32642f?w=400&h=400&fit=crop"  # 咖啡
    17 = "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop"  # 酱油
    19 = "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop"  # 醋
    20 = "https://images.unsplash.com/photo-1587322083311-b035a5e2f9d4?w=400&h=400&fit=crop"  # 蚝油
    24 = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=400&fit=crop"  # 方便面
    25 = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=400&fit=crop"  # 火锅
    43 = "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop"  # 沙琪玛
    44 = "https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=400&h=400&fit=crop"  # 黑巧
    45 = "https://images.unsplash.com/photo-1589386948082-414a873d328f?w=400&h=400&fit=crop"  # 棒棒糖
    46 = "https://images.unsplash.com/photo-1589386948082-414a873d328f?w=400&h=400&fit=crop"  # 太妃糖
    48 = "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400&h=400&fit=crop"  # 百醇
    52 = "https://images.unsplash.com/photo-1551183053-bf91b1d31191?w=400&h=400&fit=crop"  # 意面
}

Write-Host "开始下载最后14个商品图片..."
Write-Host "保存目录: $SAVE_DIR"
Write-Host "商品数量: $($IMAGE_URLS.Count)"
Write-Host "-" * 60

$success_count = 0
$fail_count = 0
$results = @()

foreach ($product_id in $IMAGE_URLS.Keys) {
    $url = $IMAGE_URLS[$product_id]
    
    # 生成文件名（使用商品ID和GUID）
    $guid = [System.Guid]::NewGuid().ToString("N")
    $filename = "product_${product_id}_${guid}.jpg"
    $save_path = Join-Path $SAVE_DIR $filename
    
    # 检查文件是否已存在
    if (Test-Path $save_path) {
        Write-Host "⊘ 文件已存在: 商品ID $product_id"
        $results += @($product_id, $filename)
        $success_count++
        continue
    }
    
    # 下载图片
    try {
        Invoke-WebRequest -Uri $url -OutFile $save_path -UserAgent "Mozilla/5.0" -TimeoutSec 30
        Write-Host "✓ 下载成功: 商品ID $product_id -> $filename"
        $results += @($product_id, $filename)
        $success_count++
    }
    catch {
        Write-Host "✗ 下载失败: 商品ID $product_id - $($_.Exception.Message)"
        $fail_count++
    }
    
    # 避免请求过于频繁
    Start-Sleep -Milliseconds 500
}

Write-Host "-" * 60
Write-Host "下载完成！成功: $success_count, 失败: $fail_count"

# 生成SQL更新语句
Write-Host "`n生成SQL更新语句:"
Write-Host "-" * 60

$sql_content = "-- 批量更新最后14个商品图片路径`n"
$sql_content += "-- 生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n`n"

for ($i = 0; $i -lt $results.Count; $i += 2) {
    $product_id = $results[$i]
    $filename = $results[$i + 1]
    $image_path = "/uploads/2026/05/19/$filename"
    $sql = "UPDATE product SET images = '$image_path' WHERE id = $product_id;"
    Write-Host $sql
    $sql_content += "$sql`n"
}

# 保存SQL到文件
$sql_file = "d:/Projects/Windsurf/GoodsManage/backend/update_final_images.sql"
$sql_content | Out-File -FilePath $sql_file -Encoding UTF8
Write-Host "`nSQL语句已保存到: $sql_file"
