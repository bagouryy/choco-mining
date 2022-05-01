# Data-mining

This repository contains the source code that was used in the experiments of the following paper : *Vernerey et al. - Threshold-free Pattern Mining Meets Multi-Objective Optimization: Application to Association Rules* ([IJCAI 2022](https://ijcai-22.org/)). Supplementary material is available in the `paper` folder.

## Requirements

- Java 8+
- Maven 3

## Installing

If you have Maven installed in your computer, you can simply build the project with the following command :

```bash
mvn clean package
```

If you are interested by using some constraints in your own project, you can use the following command :

```bash
mvn clean install
```

Now you can add a new dependency to your project :

```xml
<dependency>
    <groupId>io.gitlab.chaver</groupId>
    <artifactId>data-mining</artifactId>
    <version>1.0.0</version>
</dependency>
```

The following constraints are available :

- **AdequateClosure** : ensures that a pattern `x` is closed w.r.t. a set of measures `M` (see *Vernerey et al. - Threshold-free Pattern Mining Meets Multi-Objective Optimization: Application to Association Rules*)
- **CoverClosure** : ensures that a pattern `x` is closed w.r.t. `{freq}` (see *Schaus et al. - CoverSize : A Global Constraint for Frequency-Based Itemset Mining*)
- **CoverSize** : given an integer variable `f` and pattern `x`, ensures that `f = freq(x)` (see *Schaus et al. - CoverSize : A Global Constraint for Frequency-Based Itemset Mining*)
- **Generator** : ensures that a pattern `x` is a generator (see *Belaid et al. - Constraint Programming for Association Rules*)

Note that a `jar` file with all the required dependencies is available [here](https://drive.google.com/file/d/1o5BQb7ATyW_Ha6bJgJPYfd9BZBedQIXJ/view?usp=sharing) if you really don't want to use Maven.

## Usage

You can run the jar file using the script `run` at the root of the project. 

For each subcommand, you can list all the possible arguments using `-h` option, for example `./run closedsky -h`.

For each subcommand, you can specify an option `--tl` to limit the time of the search. For example, `--tl 60` means that the search will stop after `60` seconds if not complete. The following subcommands are available :

**closedsky/cpsky** : extract closed/sky patterns

Using the dataset `iris`, extract the skypatterns w.r.t. the set of measures `{freq(x),area(x),gr(x),mean(x.val0),max(x.val1)}`, using the weak consistency version of AdequateClosure (`--wc`), print stats of the search (`-s`), print all the skypatterns (`-p`), save result in a file named `iris_sky_fagn0M1.json`. Note that the `gr` (growth-rate) of a pattern `x` is equal to `21474836` in case of infinite value.

```bash
./run closedsky -d data/iris.dat --skym fagn0M1 --wc -s -p --json iris_sky_fagn0M1.json
```

Using the dataset `iris`, extract the skypatterns w.r.t. the set of measures `{freq(x),area(x),aconf(x)}`, using the weak consistency version of AdequateClosure (`--wc`), ignore class of the transactions (`--nc`), save result in a file named `iris_sky_fac.json`. Note that the `aconf` of a pattern `x` is multiplied by `10000`. For example, if the aconf of a pattern `x` is indicated to be `8526`, it means that the real aconf of this pattern is `0.8526`. Ignoring the class of the transactions means that the first item of each transaction will be taken into account in the mining (by default, they are ignored).

```bash
./run closedsky -d data/iris.dat --skym fac --wc --nc --json iris_sky_fac.json
```

Using the dataset `iris`, extract the closed patterns w.r.t. the set of measures `{freq(x),min(x.val0)}`, print stats of the search (`-s`), print all the patterns (`-p`).

```bash
./run cpsky -d data/iris.dat --clom fm0 -s -p
```

**arm** : association rule mining

Using the dataset `iris`, extract the Minimal Non-Redundant (`mnr`) rules, with a min relative frequency of `0.2`, a min confidence of `0.9`, print stats of the search (`-s`), print the rules (`-p`), save result in a file named `iris_mnr_20_90.json`. Note that if you want all the rules you can specify the option `--rt ar` instead.

```bash
./run arm -d data/iris.dat --rt mnr --rfmin 0.2 --cmin 0.9 -s -p --json iris_mnr_20_90.json
```

Using the dataset `iris`, extract the Minimal Non-Redundant (`mnr`) rules, w.r.t. skypatterns in the file `iris_sky_fac.json`, print stats of the search (`-s`), print the rules (`-p`).

```bash
./run arm -d data/iris.dat --rt mnr --sky iris_sky_fac.json -s -p
```

## Questions/suggestions

Feel free to contact me for any questions/suggestions related to this project : [@Charles Vernerey](mailto:charlesvernerey2@gmail.com).