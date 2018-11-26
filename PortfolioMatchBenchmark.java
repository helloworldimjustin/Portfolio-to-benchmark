import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;


public class PortfolioMatchBenchmark {
    //class variables
    private Portfolio portfolio = new Portfolio();
    private Benchmark benchmark = new Benchmark();
    private ArrayList<String> assetList = new ArrayList<>();

    /**
     * @param line is the portfolio/benchmark input
     * */
    private void getValues(String line){

        try{
            String[] assetGroup = line.split(":");//split by portfolio type (benchmark or portfolio)
            String[] portfolioGroup = assetGroup[0].split("\\|");
            String[] benchmarkGroup = assetGroup[1].split("\\|");

            for(String value : portfolioGroup){
                Asset asset = new Asset(value);
                portfolio.add(asset);

                if(!assetList.contains(asset.assetName)){assetList.add(asset.assetName);}
            }

            for(String value : benchmarkGroup){
                Asset asset = new Asset(value);
                benchmark.add(asset);

                if(!assetList.contains(asset.assetName)){assetList.add(asset.assetName);}
            }

            matchPortfolioToBenchmark();

        }catch (PatternSyntaxException pse){
            pse.printStackTrace();
            System.out.println("Please enter values in proper format");
        }
    }

    private void matchPortfolioToBenchmark(){
        TreeMap<String,Asset> orders = new TreeMap<>();
        for(String name : assetList){
            Asset portfolioAsset = portfolio.getAsset(name);
            Asset benchmarkAsset = benchmark.getAsset(name);

            if(portfolio.hasValue(name) && benchmark.hasValue(name)){
                portfolio.updateMarketValuePercentage();
                benchmark.updateMarketValuePercentage();
            }
            if(portfolioAsset.marketValuePercentage < benchmarkAsset.marketValuePercentage){
                portfolioAsset.order("BUY,"+portfolioAsset.assetName+","+(benchmarkAsset.numOfShares-portfolioAsset.numOfShares));
                orders.put(name,portfolioAsset);
            }else if(portfolioAsset.marketValuePercentage > benchmarkAsset.marketValuePercentage){
                portfolioAsset.order("SELL,"+portfolioAsset.assetName+","+(portfolioAsset.numOfShares - benchmarkAsset.numOfShares));
                orders.put(name,portfolioAsset);
            }
        }

        for(String name : orders.keySet()){
            Asset asset = portfolio.getAsset(name);
            if(asset.order!=null || asset.order.equals("")){
                System.out.println(asset.order);
            }
        }
    }

    private class Asset{
        //class variables
        String assetName;
        String assetType;
        int numOfShares;
        double marketValue;
        double assetPrice;
        double accruedInterest;
        double marketValuePercentage;
        String order;

        /**
         * @param data is a the data of an individual asset
         * */
        private Asset(String data){
            String[] asset = data.split(",");
            assetName = asset[0];
            assetType = asset[1];
            numOfShares = Integer.parseInt(asset[2]);
            accruedInterest = Double.parseDouble(asset[4]);
            assetPrice = Double.parseDouble(asset[3])+accruedInterest;

            if(assetType.equals(AssetType.STOCK.getType())){
                marketValue = numOfShares*assetPrice*AssetType.STOCK.getFactor();
            }else if(assetType.equals(AssetType.BOND.getType())){
                marketValue = (numOfShares)*assetPrice*AssetType.BOND.getFactor();
            }
        }

        private String getName(){
            return assetName;
        }

        /**
         * @param totalMarketValue is the total market value of the portfolio
         * */
        private void calculateMarketValuePercentage( double totalMarketValue){
            marketValuePercentage = marketValue/totalMarketValue;
        }

        /**
         * @param order is the BUY or SELL order to be executed
         * */
        private String order(String order){
            this.order = order;
            return this.order;
        }
    }

    private enum AssetType{
        STOCK("STOCK",1),
        BOND("BOND",0.01);

        String type;
        double factor;

        /**
         * @param type is the type of the asset
         * @param factor is the fixed factor in the marketvalue equation
         * */
        AssetType(String type, double factor){
            this.type = type;
            this.factor = factor;
        }

        private String getType(){
            return type;
        }

        private double getFactor(){
            return factor;
        }
    }

    private class Portfolio{
        //class variables
        private Hashtable<String,Asset> portfolio = new Hashtable<>();
        private double totalMarketValue = 0;

        /**
         * @param asset is the asset being stored into the portfolio
         * */
        private void add(Asset asset){
            portfolio.put(asset.getName(),asset);
        }

        private void updateMarketValuePercentage(){
            for(Asset asset : portfolio.values()){
                totalMarketValue = 0;
                for(Asset assets : portfolio.values()){
                    asset.calculateMarketValuePercentage(totalMarketValue+=assets.marketValue);
                }
            }
        }

        /**
         * @param name is the name of the asset being looked up
         * */
        private boolean hasValue(String name){
            return portfolio.containsKey(name);
        }

        /**
         * @param name is the name of the asset being looked up
         * */
        private Asset getAsset(String name){
            return portfolio.get(name);
        }

    }

    private class Benchmark{
        //class variables
        private Hashtable<String,Asset> benchmark = new Hashtable<>();
        private double totalMarketValue = 0;

        /**
         * @param asset is the asset being stored into the benchmark
         * */
        private void add(Asset asset){
            benchmark.put(asset.getName(),asset);
        }

        private void updateMarketValuePercentage(){
            for(Asset asset : benchmark.values()){
                totalMarketValue = 0;
                for(Asset assets : benchmark.values()){
                    asset.calculateMarketValuePercentage(totalMarketValue+=assets.marketValue);
                }
            }
        }

        /**
         * @param name is the name of the asset being looked up
         * */
        private boolean hasValue(String name){
            return benchmark.containsKey(name);
        }
        /**
         * @param name is the name of the asset being looked up
         * */
        private Asset getAsset(String name){
            return benchmark.get(name);
        }
    }

    public static void main(String[] args) throws IOException {
        InputStreamReader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(reader);
        String line;
        while ((line = in.readLine()) != null) {
            PortfolioMatchBenchmark pmb = new PortfolioMatchBenchmark();
            pmb.getValues(line);
        }
    }
    //GOOGLE, 10,100,.05 | AAPPL : GOOGLE, 15,

}