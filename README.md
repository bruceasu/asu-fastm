# asu-fastm
 
来自 https://sourceforge.net/projects/fastm/

修改了标识方式，添加了condition, include, asChildInclude 等有限的基本的操作。
目的还是要保持简单，尽可能不要在模板做逻辑。
如果确实喜欢在模板里做逻辑，建议用Freemark、Velocity，之类的模板实现。

# 原来的标识：
1.  动态
	```html
	<!-- BEGIN DYNAMIC: zipcodes -->
    <p>{zipcode}</p>
    <!-- END DYNAMIC: zipcodes -->
	```
	```javascript
	// BEGIN DYNAMIC: zipcodes
	console.log('{zipcode}');
	// END DYNAMIC: zipcodes 
	``` 
 
2.  变量
	```bash
	{variable}
	```
	
# 修改后：
1.  动态
	```html
	<!-- @for zipcodes -->
    <p>${zipcode}</p>
    <!-- @done zipcodes -->
	
    <!-- @if zipcode==123456 -->
        <p>${zipcode}</p>
    <!-- @end zipcode -->
    
	<!-- @# comment -->
    this is comment text.
    <!--@% comment -->     
    ```
    	
	```javascript
	// @for zipcodes
	console.log('${zipcode}');
	// @done zipcodes

	// @if zipcode != ""
	console.log('${zipcod}');
	// @end zipcode

	// @# comment
    var x = "this is comment text. ";
    // @% comment     
    
	``` 
 
2.  变量
	```bash
	${variable}
    ${global_value}
    ${include file [encoding]}
    ${asChildInclude name file [encoding]}
	```
	




