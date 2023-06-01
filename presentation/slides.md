---
theme: ./theme

# https://sli.dev/custom/highlighters.html
highlighter: shiki

# show line numbers in code blocks
lineNumbers: false

# some information about the slides, markdown enabled
info: |
  ## Slidev Starter Template
  Presentation slides for developers.

  Learn more at [Sli.dev](https://sli.dev)

# persist drawings in exports and build
drawings:
  persist: false

# use UnoCSS
css: unocss

layout: intro
background: /images/devoxx-template.png
dim: false
---

<div class="ml-60px">

# Thawing Java on AWS Lambda
## Reducing cold start times from 11 seconds to 1

<div class="text-black">
Daniel Raniz Raneland<br />
Coding Architect @ factor10

<ul class="list-none! columns-2">
  <li><mdi-email />raniz@factor10.com</li>
  <li><mdi-github />Raniz85</li>
  <li><mdi-mastodon />raniz@mastodon.online</li>

  <li><mdi-firefox />raniz.blog</li>
  <li><mdi-linkedin />/in/raneland</li>
  <li><mdi-gitlab />raniz</li>
  <li>&nbsp;</li>
</ul>
</div>
</div>

---
layout: intro
background: /images/devoxx-template.png
dim: false
---

<div class="ml-60px">

# Thawing Java on AWS Lambda
<h2 class="decoration-line-through">Reducing cold start times from 11 seconds to 1</h2>

<div class="text-black">
Daniel Raniz Raneland<br />
Coding Architect @ factor10

<ul class="list-none! columns-2">
  <li><mdi-email />raniz@factor10.com</li>
  <li><mdi-github />Raniz85</li>
  <li><mdi-mastodon />raniz@mastodon.online</li>

  <li><mdi-firefox />raniz.blog</li>
  <li><mdi-linkedin />/in/raneland</li>
  <li><mdi-gitlab />raniz</li>
  <li>&nbsp;</li>
</ul>
</div>
</div>

---
layout: intro
background: /images/devoxx-template.png
dim: false
---

<div class="ml-60px">

# Thawing Java on AWS Lambda
## Reducing cold start times from 6 seconds to .1

<div class="text-black">
Daniel Raniz Raneland<br />
Coding Architect @ factor10

<ul class="list-none! columns-2">
  <li><mdi-email />raniz@factor10.com</li>
  <li><mdi-github />Raniz85</li>
  <li><mdi-mastodon />raniz@mastodon.online</li>

  <li><mdi-firefox />raniz.blog</li>
  <li><mdi-linkedin />/in/raneland</li>
  <li><mdi-gitlab />raniz</li>
  <li>&nbsp;</li>
</ul>
</div>
</div>

---

# About Raniz


<div class="relative w-full h-full"><v-clicks>

  <div class="absolute top-50 left-10">
  &gt;15 years in the industry
  </div>
  <div class="absolute top-75 left-25">
  Masters degree from LTH
  </div>
  <div class="absolute top-100 left-150">
  Sony Mobile Tokyo
  </div>
  <div class="absolute top-15 left-80">
  Spiideo
  </div>
  <div class="absolute top-65 left-120">
  Factor10
  </div>
  <div class="absolute top-30 left-120">
  2 kids, 1 wife, 0 pets
  </div>
  <div class="absolute top-5 left-150">
  Triathlete?
  </div>

</v-clicks></div>

---
layout: center
---

![Lambda Function Overview](/images/function-overview.png)

---
layout: center
---

![Spring Logo](/images/spring-logo.png)

<Attribution>
Logo from Wikimedia Commons, Apache License 2.0
</Attribution>

---
layout: center
---

![Call Graph](/images/call-graph-warm.png)

---
layout: center
---

<ScatterPlot dataFile="spring-warm.json" />

<Arrow v-click class="text-red" x1="200" y1="90" x2="105" y2="103" />

<!--
Outlier in the beginning of ~6.5 seconds, the rest are sub-50 ms
-->

---
layout: cover
dim: false
background: /images/frozen-cars.jpg
---

<Attribution>
Santiago Puig Vilado, CC-BY-SA 3.0 via Wikimedia Commons
</Attribution>

<!--
Cold starts, which is why we are here
-->

---
layout: center
clicks: 10
---
![Lambda Example Arch](/images/aws-lambda-architecture.png)

<Attribution>
Image from AWS Documentation
</Attribution>

<div v-if="[1, 3, 4, 6, 7, 9, 10].includes($slidev.nav.clicks)" class="absolute red-box left-305px top-330px w-115px h-90px"></div>

<div v-if="$slidev.nav.clicks === 1" class="absolute red-box left-450px top-180px w-155px h-290px"></div>

<div v-if="$slidev.nav.clicks === 4" class="absolute red-box left-450px top-270px w-155px h-90px"></div>

<div v-if="$slidev.nav.clicks === 7" class="absolute red-box left-450px top-380px w-155px h-90px"></div>

<div v-if="$slidev.nav.clicks === 10" class="absolute red-box left-450px top-180px w-155px h-90px"></div>

<!--
4 lambdas:
- authorizer
- show
- info
- tickets

Worst case you're the first visitor and get hit by four cold starts

-->

---
layout: center
---

![Call Graph](/images/call-graph-warm.png)

---
layout: center
---

![Call Graph](/images/call-graph-cold.png)

<div class="absolute top-180px left-320px w-90px h-110px border-red border-3 rounded-5"></div>

---

# Benchmarking Cold Starts

1. Update lambda configuration
2. Wait for lambda to be ready
3. Run request
4. Ensure that the log tail contains initialization time 
    a. store if it does
    b. discard if it does not
5. Repeat until we have enough measurements

---

# Cold Start Benchmarks

<ScatterPlot dataFile="spring.json" />

---
layout: cover
dim: false
background: /images/antifreeze.jpg
---

<Attribution>
Robineero, CC-BY-SA 4.0 via Wikimedia Commons
</Attribution>

<!--
We need some antifreeze for our code.

But what is that?
-->

---

# Spring startup

1. Scan classpath
2. Figure out how to wire everything up
3. Wire everything up

---
layout: center
---

![Micronaut logo](/images/micronaut-logo.png)

---
layout: statement
---

# The Micronaut way

Your application startup time and memory consumption aren’t bound to the size of your codebase,
resulting in a _monumental leap in startup time_,
blazing fast throughput,
and a minimal memory footprint.

---

# Compile Time vs Runtime

<div class="text-center">
Spring, analyze at startup
<img src="/images/spring-runtime.png" />
</div>

<div v-click class="text-center">
Micronaut, analyze at compile time
<img src="/images/micronaut-runtime.png" />
</div>

---
layout: two-cols
---

# Very similar to work with

::left::

## Spring

```java
@Component
public class UserRequestHandler
        implements Function<CreateUser, User> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User apply(@Validated CreateUser input) {
```

::right::

## Micronaut

```java
@Introspected
public class UserRequestHandler
        extends MicronautRequestHandler<CreateUser, User> {

    @Inject
    private UserRepository userRepository;

    @Override
    public User execute(@Valid CreateUser input) {
```

---
layout: two-cols
---

# Very similar to work with

::left::

## Spring

```java
@Component
public class UserRepository {
    private final DynamoDbClient dynamo;
    private final String tableName;
    
    
    public UserRepository(
            DynamoDbClient dynamo,
            @Value("${user.table-name}")
            String tablename
    ) {
```

::right::

## Micronaut

```java
@Singleton
public class UserRepository {
    private final DynamoDbClient dynamo;
    private final String tableName;

    @Inject
    public UserRepository(
            DynamoDbClient dynamo,
            @Property(name="user.table-name")
            String tablename
    ) {
```

---

# Cold Start Benchmarks

<ScatterPlot dataFile="micronaut.json" />

---
layout: statement
---

# What's the real problem?

---

# Compile Time vs Runtime

<div class="text-center">
Spring, analyze at startup
<img src="/images/spring-runtime.png" />
</div>

<div class="text-center">
Micronaut, analyze at compile time
<img src="/images/micronaut-runtime.png" />
</div>

---

# Compile Time vs Runtime

<img class="top-50% translate-y-160px" src="/images/runtime-jit.png" />

---

# Just in Time Compilation

<div>
    <div class="text-center">Spring, JIT at startup, analyze at startup</div>
    <img src="/images/spring-runtime-jit.png" />
</div>

<div class="py-8">
    <div class="text-center">Micronaut, analyze at compile time, JIT at startup</div>
    <img src="/images/micronaut-runtime-jit.png" />
</div>

---

# Lambda SnapStart

## On Publish

- Start JVM, doing JIT and initialization
- Take snapshot

## On "Cold Start"

- Restore snapshot
- Call function

<!--
Launched in 2022.
-->

---

# Cold Start Benchmarks

<ScatterPlot dataFile="snap.json" />

---

# Just in Time Compilation

![JIT Overview](/images/spring-micronaut-jit.png)

---
layout: center
---

<img src="/images/rust-logo-blk.svg" class="h-400px" />

---

# Rust looks very different from Java

```rust
#[derive(Serialize)]
struct User {
    id: Uuid,
    name: String,
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let func = service_fn(func);
    lambda_runtime::run(func).await?;
    Ok(())
}

async fn func(event: LambdaEvent<CreateUser>) -> Result<User, Error> {
    let (user, _context) = event.into_parts();
    let config = aws_config::load_from_env().await;
    let user = User {
        id: Uuid::new_v4(),
        name: user.name
    };
    let client = Client::new(&config);
    client.put_item()
```

---

# Cold Start Benchmarks

<ScatterPlot dataFile="rust.json" />

<!--
Rust averages at around 80 ms
-->

---
layout: center
---

![GraalVM logo](/images/graalvm-logo.png)

---
layout: center
---

![GraalVM features](/images/graalvm-features.png)

---

# GraalVM vs JVM

<div>
    <div class="text-center">Spring, JIT at startup, analyze at startup</div>
    <img src="/images/spring-runtime-jit.png" />
</div>

<div class="py-8">
    <div class="text-center">Micronaut, analyze at compile time, JIT at startup</div>
    <img src="/images/micronaut-runtime-jit.png" />
</div>

<div v-click>
    <div class="text-center">"GraalVM, native compile ahead of time</div>
    <img src="/images/graalvm-runtime.png" />
</div>

---

# Cold Start Benchmarks

<ScatterPlot dataFile="micronaut-native.json" />

<!--
Micronaut Native averages at around 120 ms
-->

---
layout: statement
---

# Drawbacks

---
layout: center
---

![XKCD: Compiling](/images/compiling.png)

<Attribution>
Randal Munroe, https://xkcd.com/303 CC BY-NC 2.5
</Attribution>

---
layout: cover
dim: false
background: /images/php-hammer.jpg
---

<Attribution>
Ian Baker, flickr.com/photos/raindrift/ CC BY 2.0
</Attribution>

<!--
Tools that work with the JVM won’t work with native images. Debugging is done with GDB - which isn’t supported by IDEA for example.
-->

---
layout: cover
dim: false
background: /images/square-hole.jpg
---

<Attribution>
Simon Greig, flickr.com/photos/xrrr/ CC BY-NC-SA 2.0
</Attribution>

<!--
Native binaries are not portable. Not really “Write Once Run Anywhere” anymore. Cross-compilation support is currently non-existent.
The Micronaut plugin for Gradle uses Docker to build for AmazonLinux
-->

---

# Raniz' Flowchart for Thawing Your Lambdas

<div class="mt-15">

![Flowchart for deciding on how to fix your lambda](/images/flowchart.png)

</div>
