# 答辩问题
### 1.小程序的登录模块是怎么实现的？
小程序前端调用login.vue中的登录方法，其中有utils/request.js文件进行统一封装后传递到后端。后端调用service/AuthService.java中的登录方法，根据手机号查询用户表，并使用ByCrypt进行密码验证，判断账号是否正常，使用sa-token生成token，登录成功后返回token，角色，用户信息给前端。前端通过store/user.js保存信息到本地，并在后续通过utils/request.js让后续请求自动携带token。
### 2.预约回收功能如何实现？
小程序前端调用create.vue中的方法handleSubmit,填写相关参数后通过requset.js进行封装，通过sa-token获取当前用户ID，后端调用service/RecycleOrderService.java中的方法创建订单实体，并保存到数据库中。
### 3.数据库表之间的关系是什么？
用户表和订单表是1对多的关系，与回收员与机构表构成1对1的关系。回收员，机构与订单表形成1对多的关系，订单与评价形成1对1的关系，与投诉形成1对多的关系。
### 4.小程序图片上传功能如何实现？
小程序前端通过create.vue中的方法choosePhoto上传图片，由request.js进行封装传递到后端。后端在contorller/CommonController.java对图片进行验证并上传到目录。
### 5.网页端导出EXCEL功能如何实现？
位于后台管理页面的order/OrderView.vue,调用handleExport方法,将Orders.value中的数据转为有表头的数据.最后使用xlsx库中的 方法进行导出。
### 6.网页端查询搜索功能如何实现？
前端通过getOrders调用接口把数据传递给后端,后端AdminContoller接收数据并调用AdminOrderService的list方法查询数据库并返回结果。
### 7.网页端的分页怎么实现？
通过OrderView.vue中的分页组件,通过定义分页状态,在加载页面时将请求参数放入.传递到后端的AdminOrderController中,调用AdminOrederService.java中的方法进行分页查询并返回结果。
### 8.小程序扫码接收功能如何实现？
调用service/CollectorOrderService.java中的方法，用QRUtil类generateQRCode方法进行编码。

### 9.系统前后端分离与不分离的区别？什么叫前后端分离？
前端负责页面展示和用户交互,后端负责业务操作和数据接口,前后端通过接口通信。
### 10.为什么使用vue？vue有什么特点和优势？
vue具有数据驱动，组件化，开发效率高的特点。它可以把页面拆分成多个独立组件，便于复用和维护。同时它适合前后端开发，前端通过接口与后端通信，使其页面和数据分离。
### 11.视图层，控制层，业务层，持久层的作用？
视图层：负责页面的渲染，用户交互，数据展示

控制层：负责处理用户请求，调用业务层，返回结果给视图层

业务层：负责处理业务逻辑，调用持久层，返回结果给控制层

持久层：负责数据访问，调用数据库，返回结果给业务层
### 12.什么是json，wxss？
json是一种数据和配置格式，wxss是一种样式格式。在我的系统中，json一般用于配置页面路径和导航栏信息，wxss用于定义页面的样式。