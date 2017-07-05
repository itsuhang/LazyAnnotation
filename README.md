# LazyAnnotation
使用apt注解偷懒
1.使用注解生成Dagger2的Component,并可以有子组件(Subcomponent)
同时可以生成DaggerHelper帮助类,帮助简化注入过程

2.SharedPreferences持久化注解
通过@SharedPreferences注解,并在Application中init()
会生成xxxHelper(xxx为注解的类名)

3.@FindMethod
用于帮助查找Retrofit的Service所对应的方法(根据传入的子URL)


基本都是用来偷懒的工具

