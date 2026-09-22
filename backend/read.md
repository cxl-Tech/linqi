启动步骤

修复完成后，按以下步骤运行：

后端（需要 JDK 8+ 和 Maven）:
cd backend
mvn clean install -DskipTests
mvn spring-boot:run

管理后台前端:
cd frontend-admin
npm run dev     # 端口 5174

消费者前端:
cd frontend-consumer
npm run dev     # 端口 5173

项目启动后，先初始化数据库（MySQL，数据库名 expiry_food_db），然后访问对应端口即可。