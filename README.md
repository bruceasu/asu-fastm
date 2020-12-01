# asu-fastm
 
来自 https://sourceforge.net/projects/fastm/

修改了标识方式，添加了condition, include, asChildInclude 等有限的基本的操作。
目的还是要保持简单，尽可能不要在模板做逻辑。
如果确实喜欢在模板里做逻辑，建议用 Freemark、 Velocity，之类的模板实现。

从使用上来讲DYNAMIC几乎满足了所有情形： 
1. null: 不显示
2. 单个对象（map）： 显示一次
3. 多个对象（List）： 循环显示

即使无法表达的 当 null 时，显示的条件，也是可以通过变通的方式处理，
例如： a == null 时，设置 b = Map.
```html
<!-- BEGIN DYNAMIC: a -->
	当 a == null 时，这部分不显示。
<!-- END DYNAMIC: a -->

<!-- BEGIN DYNAMIC: b -->
	当 a == null 时, 但因b有值，所以显示。
<!-- END DYNAMIC: b -->
```

只是这样一来，语意的表达就有的怪异，特别是在多分支时，更是费解。
为了语意上的清晰，还是引入条件处理（CONDITION），在模板做有限的计算。

引入include, asChildInclude 也是为了让模板更加清晰。不建议拆成一堆小文件，为拆分而拆分。

# 原来的标识：
1.  动态
	```html
	<!-- BEGIN DYNAMIC: zipcodes -->
    <p>{zipcode}</p>
    <!-- END DYNAMIC: zipcodes -->
   
    <!-- BEGIN IGNORED: comment -->
    this is comment text.
    <!-- END IGNORED: comment -->     
	```
	
	```javascript
	// BEGIN DYNAMIC: zipcodes
	console.log('{zipcode}');
	// END DYNAMIC: zipcodes 
	
	// BEGIN IGNORED: comment
    var x = "this is comment text. ";
    // END IGNORED: comment     
    
	``` 
 
2.  变量
	```bash
	{variable}
	```
	
# 修改后：
1.  动态
	```html
	<!-- for zipcodes -->
    <p>${zipcode}</p>
    <!-- done zipcodes -->
	
    <!-- when zipcode==123456 -->
        <p>${zipcode}</p>
    <!-- end zipcode==123456 -->
    
	<!-- # comment -->
    this is comment text.
    <!-- ## comment -->     
    ```
    	
	```javascript
	// for zipcodes
	console.log('${zipcode}');
	// done zipcodes

	// when zipcode!=""
	console.log('${zipcod}');
	// end zipcode!=""

	// # comment
    var x = "this is comment text. ";
    // ## comment     
    
	``` 
 
2.  变量
	```bash
	${variable}
    ${global_value}
    ${include file [encoding]}
    ${asChildInclude name file [encoding]}
	```
	




