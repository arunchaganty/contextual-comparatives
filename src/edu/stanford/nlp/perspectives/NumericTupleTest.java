package edu.stanford.nlp.perspectives;

import org.junit.Test;

import static edu.stanford.nlp.util.logging.Redwood.Util.endTrack;
import static edu.stanford.nlp.util.logging.Redwood.Util.log;
import static edu.stanford.nlp.util.logging.Redwood.Util.startTrack;

/**
 * Tests for behavior of numeric tuples
 */
public class NumericTupleTest {

  public static final NumericTuple nasa =
      new NumericTuple("the NASA budget", "is", 17e9, "USD");

  public static final NumericTuple usBudget =
      new NumericTuple("the US budget", "is", 17e9 * 200, "USD");

  public static final NumericTuple militaryBudget =
      new NumericTuple("the US military budget","is",17e9 * 30,"USD");

  public static final NumericTuple foodCost  =
      new NumericTuple("food cost", "is", 300, "USD per month per person");
  public static final NumericTuple foodCostYr  =
      new NumericTuple("food cost", "is", 300*12, "USD per year per person");
  public static final NumericTuple LA  =
      new NumericTuple("LA", "has", 3.8e6, "person");

  public static final NumericTuple cars_in_US  =
      new NumericTuple("the US", "has", 250e6, "car");
  public static final NumericTuple cars_in_India  =
      new NumericTuple("India", "has", 50e6, "car");
  public static final NumericTuple people_in_US  =
      new NumericTuple("the US", "has", 320e6, "person");
  public static final NumericTuple LAtoSF =
      new NumericTuple("LA", "has the distance of to SF of", 380, "mi");
  public static final NumericTuple gasMileage =
      new NumericTuple("driving", "consumes", 0.05, "gallon per mi");
  public static final NumericTuple worldProd =
      new NumericTuple("the world", "produces", 80e6, "barrel per day");
  public static final NumericTuple worldProdYr =
      new NumericTuple("the world", "produces", 80e6 * 365, "barrel per year");
  public static final NumericTuple aYear =
      new NumericTuple("a year", "is", 1, "year");

  public static final NumericTuple isis  =
      new NumericTuple("ISIS", "occupies", 36e3, "sq. mi.");
  public static final NumericTuple syria  =
      new NumericTuple("Syria", "has area", 72e3, "sq. mi.");



  @Test
  public void testNasaSimple() {
    startTrack("simple");
    log(nasa.toString());
    log(nasa.comparePercent(0.005, usBudget));
    endTrack("simple");
  }

  @Test
  public void testNasaMilitary() {
    startTrack("The 2015 NASA budget is 1/30th of the US military budget.");
    log(militaryBudget.comparisonNP(nasa));
    endTrack("The 2015 NASA budget is 1/30th of the US military budget.");
  }

  @Test
  public void testCars() {
    startTrack("The US has 5 times as many cars as India.");
    log(cars_in_India.comparisonNP(cars_in_US));
    endTrack("The US has 5 times as many cars as India.");
  }

  @Test
  public void testCarsPeople() {
    startTrack("The US has 2 people per car.");
    log(people_in_US.div(cars_in_US));
    endTrack("The US has 2 people per car.");

    startTrack("The US has 1 car per two person.");
    log(people_in_US.div(cars_in_US));
    endTrack("The US has 1 car per two person.");
  }

  @Test
  public void testCarsPeopleComplex() {
    startTrack("The US has 1 car per two persons.");
    log(cars_in_US.div(people_in_US.unitChange("person", 0.5, "two-person")));
    endTrack("The US has 1 car per two persons.");
  }


  @Test
  public void testISISSyria() {
    startTrack("ISIS occupies the area of Syria.");
    log(syria.comparisonNP(isis));
    endTrack("ISIS occupies the area of Syria.");
  }

  @Test
  public void LAfood() {
    startTrack("The food cost for LA is 13e9 USD / yr");

    log(foodCost.unitChange("per month", 12., "per year").mul(LA));

    endTrack("The food cost for LA is 13e9 USD / yr");
  }

  @Test
  public void gasProd() {

    final NumericTuple left =
        cars_in_US.unitChange("car", 52, "trip per year")
        .mul(gasMileage.mul(LAtoSF.unitChange("", 1, "per trip")));
    final NumericTuple right = worldProd.unitChange("barrel", 20, "gallon").unitChange("per day", 365, "per year");

    startTrack("Driving from LA to SF consumes 190 gallons per trip.");
    log(gasMileage.mul(LAtoSF.unitChange("",1, "per trip")));
    endTrack("Driving from LA to SF consumes 190 gallons per trip.");

    startTrack("The US driving from LA to SF 104 times consumes 11.6B gallons per year.");
    log(left);
    endTrack("The US driving from LA to SF 104 times consumes 4.9B gallons per year.");
    startTrack("The world produces 5.8B gallons per year.");
    log(right);
    endTrack("The world produces 5.8B gallons per year.");

    startTrack("If everyone in the US drove from LA to SF once a week, we would consume more gas than produced by the world every year.");
    log(left.compare(right));
    log(right.comparisonNP(left));
    log(left.comparisonNP(right));
    endTrack("If everyone in the US drove from LA to SF once a week, we would consume more gas than produced by the world every year.");
  }

  @Test
  public void income() {
    NumericTuple life = NumericTuple.of("people", "live for", 4.15e+07, "time");
    NumericTuple income = NumericTuple.of("median income", "is", 9.87e-02, "USD per time");
    NumericMention mention = NumericMention.of(10e6, "USD");

    log(life.mul(income).comparisonNP(mention));
    log(income.mul(life).comparisonNP(mention));
  }

  @Test
  public void volume() {
    NumericTuple life = NumericTuple.of("people", "live for", 4.15e+07, "time");
    NumericTuple income = NumericTuple.of("median income", "is", 9.87e-02, "USD per time");
    NumericMention mention = NumericMention.of(10e6, "USD");

    log(life.mul(income).comparisonNP(mention));
    log(income.mul(life).comparisonNP(mention));
  }

}
