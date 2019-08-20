package script;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.LoginResponseListener;
import org.rspeer.runetek.event.types.*;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.Script;
import org.rspeer.script.events.LoginScreen;
import script.automation.data.LaunchedClient;
import script.data.*;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.tanner.ExPriceChecker;
import script.tanner.Main;
import script.tasks.*;
import script.ui.Gui;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.List;

import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.INVALID_CREDENTIALS;
import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.RUNESCAPE_UPDATE_2;

@ScriptMeta(name = "Ultimate Beggar", desc = "Begs for gp", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener, ChatMessageListener, LoginResponseListener {

    public int startC = -1;
    public StopWatch runtime;
    public List<Coins> gpArr;
    public Coins gp;
    public Location location;
    public Lines lines;
    public int preTradeGP = 0;
    public boolean walk = true;
    public boolean beg = true;
    public boolean tradePending = false;
    public boolean trading = false;
    public String traderName = "";
    public boolean changeAmount = false;
    public boolean iterAmount;
    public String muleName;
    public int muleAmnt = Integer.MAX_VALUE;
    public int minWait;
    public int maxWait;
    public int muleKeep;
    public int amountChance;
    public boolean isMuling = false;
    private String[] linesArr;
    public boolean defaultLines = false;
    public String inputLines;
    public int muleWorld;
    public boolean buildGEPath = true;
    public int worldPop;
    public boolean worldHop;
    public int hopTime;
    public boolean hopTimeExpired = false;
    public int sameTraderCount = 0;
    public int walkChance;
    public boolean worldHopf2p;
    public boolean equipped = false;
    public int currWorld = -1;
    public Player trader = null;
    public int hopTryCount = 0;
    public int amntIndex = 0;
    public boolean processSentTrade = false;
    public boolean tradeSent = false;
    public boolean sendTrade = true;
    public int sendTryCount = 0;
    public boolean sentTradeInit = false;
    public int gainedC = 0;
    public int amntMuled = 0;
    public boolean banked = false;
    public boolean bought = false;
    public boolean disableChain = true;
    public int randBuyGP = randInt(1500, 3500);
    public boolean atMinPop = false;
    // ADD TO GUI
    public boolean setSendTrades = false;
    private int genTries = 0;
    public final int[] items = new int[]{1117, 1115, 1139, 1155, 1153, 1137, 1067};
    public int item = items[randInt(0, items.length - 1)];
    public StopWatch lastTradeTime;
    public boolean refreshPrices = false;
    public long startTime = 0;
    public static final String CURR_WORLD_PATH = Script.getDataDirectory() + "\\CurrBegWorld.txt";
    public static final String ERROR_FILE_PATH = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeerErrors.txt";
    public boolean tradeGambler = false;
    public boolean roll = false;
    public int gambleAmnt = 0;
    public String gamblerName = "";
    public boolean giveGambler = false;
    private static final String PYTHON_3_EXE = "C:\\Users\\bllit\\AppData\\Local\\Programs\\Python\\Python37\\python.exe";
    private static final String ACC_GEN_PY = "C:\\Users\\bllit\\IdeaProjects\\OSRS-Account-Generator\\create_rs_account.py";
    private static final String PASSWORD_ARG = "-p plmmlp";
    public ArrayList<Integer> OTHER_BEG_WORLDS;
    private final boolean GAMBLER = false;
    public Main tanner;
    public boolean isTanning = false;
    public final Area TUTORIAL_ISLAND_AREA = Area.rectangular(3049, 3139, 3161, 3057);
    public boolean startupChecks = false;
    public List<LaunchedClient> runningClients;
    public int[] popWorldsArr = new int[]{301, 308, 393};
    public int minPop = 250;
    public boolean muted = false;
    public MuleArea muleArea;
    private int[] lastPrices = new int[4];
    public boolean startChocBeg = false;
    public boolean muleChocBeg = false;
    public static final int SAVE_BEG_GP = 10000;
    public script.chocolate.Main chocolate;
    public boolean isChoc = false;



    public static final String MULE_NAME = "IBear115";
    public static final MuleArea MULE_AREA = MuleArea.COOKS_GUILD;
    public static final int MULE_WORLD = 393;
    public static final boolean MULE_ITEMS = false;
    public static final int MUTED_MULE_AMNT = 25000;
    public static final int ALLOWED_INSTANCES = 8;
    public final String API_KEY = "JV5ML4DE4M9W8Z5KBE00322RDVNDGGMTMU1EH9226YCVGFUBE6J6OY1Q2NJ0RA8YAPKO70";
    public static final int NUM_BACKLOG_ACCOUNTS = 40;
    public static final int START_CB_AMNT = 5500000;
    public static final boolean BUY_GEAR = true;

    @Override
    public void onStart() {
        Log.fine("Script Started");
        LoginScreen ctx = new LoginScreen(this);
        ctx.setDelayOnLoginLimit(true);
        ctx.setStopScriptOn(LoginResponseEvent.Response.ACCOUNT_DISABLED, true);
        ctx.setStopScriptOn(LoginResponseEvent.Response.ACCOUNT_LOCKED, true);
        ctx.setStopScriptOn(LoginResponseEvent.Response.RUNESCAPE_UPDATE, true);
        ctx.setStopScriptOn(RUNESCAPE_UPDATE_2, true);

        runtime = StopWatch.start();
        startC = Inventory.getCount(true, 995);
        location = Location.GE_AREA;

        submit(new Gui(this));
        muleName = MULE_NAME;
        muleWorld = MULE_WORLD;
        muleArea = MULE_AREA;

        startTime = (worldHop || worldHopf2p) ? System.currentTimeMillis() : 0;
        setRandMuleKeep(2500, 10000);

        chocolate = new script.chocolate.Main(this);
        if (startC > START_CB_AMNT)
            startChocBeg = true;

        if (GAMBLER) {
            /*submit(new GTradePlayer(),
                    new GWaitTrade(),
                    //new Mule(),
                    new GSendTrade(),
                    new GRoll(),
                    new WorldHop(this),
                    new Banking(this),
                    new GTraverse(),
                    new Gambler()
            );*/
        } else {
            submitTasks();
        }
    }

    private void submitTasks() {
        submit(new StartupChecks(this),
                new TradePlayer(this),
                new SellGE(chocolate, this),
                new BuyGE(chocolate, this),
                new WaitTrade(this, chocolate),
                new StartOther(this),
                new Mule(this, chocolate),
                new WorldHop(this),
                new ChangeAmount(this),
                new ToggleRun(this),
                new Banking(this),
                new Traverse(this),
                new BuyEquip(this),
                new Beg(this),
                new SendTrade(this)
        );
    }

    public void restartBeggar() {
        removeAll();
        banked = false;
        changeAmount = true;
        walk = true;
        beg = true;
        buildGEPath = true;
        trading = false;
        equipped = false;
        bought = false;
        randBuyGP = Beggar.randInt(1500, 5000);
        isMuling = false;
        startTime = (worldHop || worldHopf2p) ? System.currentTimeMillis() : 0;

        submitTasks();
    }

    @Override
    public void notify(LoginResponseEvent loginResponseEvent) {
        if (loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_DISABLED) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_STOLEN) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_LOCKED) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.RUNESCAPE_UPDATE) ||
                loginResponseEvent.getResponse().equals(RUNESCAPE_UPDATE_2) ||
                loginResponseEvent.getResponse().equals(INVALID_CREDENTIALS)
        ) {
            disableChain = false;
            setStopping(true);
        }
    }

    @Override
    public void onStop() {
        Log.severe("Script Stopped");
        removeAll();

        if (isMuling || (isTanning && tanner.isMuling) || (isChoc && chocolate.isMuling)) {
            Mule.logoutMule();
        }

        if (currWorld != -1 && !isTanning && !isChoc) {
            Log.info("World Removed");
            removeCurrBegWorld(currWorld);
        }

        if (!disableChain && !GAMBLER) {
            Log.fine("Chaining");
            try {
                Thread.sleep(randInt(0, 600000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int[] IDs = writeJson(readAccount());

            generateAccount(NUM_BACKLOG_ACCOUNTS);

            String path1 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape";
            String path2 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar";
            int sleep = randInt(900000, 1200000);
            String javaVersion = "java";//"\"C:\\Program Files\\Java\\jdk1.8.0_201\\bin\\java.exe\"";
            String launcher = javaVersion + " -jar C:\\Users\\bllit\\OneDrive\\Desktop\\BegLauncher.jar "
                    + IDs[0] + " " + IDs[1] + " " + path1 + " " + path2 + " " + sleep + " && exit";

            try {
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + launcher + "\"");

                System.exit(0);

            } catch (Exception e) {
                System.out.println("HEY Buddy ! U r Doing Something Wrong ");
                e.printStackTrace();
            }
        }
    }

    private void executeGenerator() {
        String randEmailArg = "-e " + getRandString() + "@gmail.com";

        try {
            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + PYTHON_3_EXE + " " + ACC_GEN_PY + " " + randEmailArg + " " + PASSWORD_ARG + " && exit" + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getRandString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        int strLen = randInt(12, 18);

        while (salt.length() < strLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    public void generateAccount(int setNumBacklogged) {
        while (genTries < setNumBacklogged / 2 && getNumAccsBacklogged() < setNumBacklogged) {
            executeGenerator();
            genTries++;

            try {
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                Time.sleep(180000);
                e.printStackTrace();
            }
        }
    }

    public int[] writeJson(String account) {
        File file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar1.json");
        File file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape1.json");
        String data1 = "";
        String data2 = "";

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data1 = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr1 = data1.split(System.lineSeparator());
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i].contains("\"RsUsername\":")) {
                arr1[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
            if (arr1[i].contains("\"World\":")) {
                arr1[i] = "\t\t\"World\": " + popWorldsArr[randInt(0, 2)] + ",";
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file2))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data2 = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr2 = data2.split(System.lineSeparator());
        for (int i = 0; i < arr2.length; i++) {
            if (arr2[i].contains("\"RsUsername\":")) {
                arr2[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
            if (arr1[i].contains("\"World\":")) {
                arr1[i] = "\t\t\"World\": " + popWorldsArr[randInt(0, 2)] + ",";
            }
        }

        int[] IDs = new int[2];
        PrintWriter pw = null;
        try {
            int beggarID = 1;
            while (file1.exists()) {
                beggarID++;
                file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar" + beggarID + ".json");
            }
            file1.createNewFile();
            IDs[0] = beggarID;

            pw = new PrintWriter(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr1) {
            assert pw != null;
            pw.println(s);
        }
        assert pw != null;
        pw.close();

        PrintWriter pw2 = null;
        try {
            int simscapeID = 1;
            while (file2.exists()) {
                simscapeID++;
                file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape" + simscapeID + ".json");
            }
            file2.createNewFile();
            IDs[1] = simscapeID;

            pw2 = new PrintWriter(file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr2) {
            assert pw2 != null;
            pw2.println(s);
        }
        assert pw2 != null;
        pw2.close();

        return IDs;
    }

    private static final String ACCOUNTS_FILE_PATH = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt";
    private List<String> accountsList;

    public String readAccount() {
        String accounts = "";
        File file = new File(ACCOUNTS_FILE_PATH);

        if (!file.exists()) {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                readAccount();
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                accounts = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
            readAccount();
        }

        //file.delete();

        accounts = accounts.trim();
        accountsList = Arrays.asList(accounts.split(System.lineSeparator()));
        String account = accountsList.get(0);
        account = account.trim();

        writeAccounts(file);

        return account;
    }

    private void writeAccounts(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < accountsList.size(); i++) {
            if (pw != null) {
                pw.println(accountsList.get(i));
            }
        }
        if (pw != null) {
            pw.close();
        }
    }

    private int getNumAccsBacklogged() {
        int numAccsBacklogged = 0;
        File file = new File(ACCOUNTS_FILE_PATH);

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();

                while (line != null && line.contains("@")) {
                    numAccsBacklogged++;
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            Log.info("File not found");
        }

        return numAccsBacklogged;
    }

    @Override
    public void notify(ChatMessageEvent msg) {
        // If not in a trade and a player trades you...
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE) && !isMuling) {
            if (msg.getSource().equals(traderName)) {
                sameTraderCount ++;
            } else {
                sameTraderCount = 0;
            }
            if (sameTraderCount < 5) {
                traderName = msg.getSource();
                tradePending = true;
                trading = true;
                walk = false;
                beg = false;
            }
        }

        if ((msg.getMessage().contains("Sending trade offer") || msg.getType().equals(ChatMessageType.TRADE_SENT)) &&
                !isMuling && !Trade.isOpen() && !trading) {
            //Log.info("Trade sent");
            tradeSent = true;
        }

        if (msg.getMessage().contains("player is busy at the moment") && !isMuling && !Trade.isOpen() && !trading) {
            //walk = true;
            //beg = true;
        }
    }

    @Override
    public void notify(RenderEvent e) {

        Graphics g = e.getSource();
        gainedC = Inventory.getCount(true, 995) + amntMuled;
        gainedC -= startC;
        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 40);
        g.drawString(lastTradeTime == null ? "Last trade completed: " + "00:00:00" : "Last trade completed: " + lastTradeTime.toElapsedString(), 20, 60);
        g.drawString("Gp gained: " + format(gainedC), 20, 80);
        g.drawString("Gp /h: " + format((long) runtime.getHourlyRate(gainedC)), 20, 100);
        g.drawString("Times tanned: " + timesTanned, 20, 120);

        if (isTanning) {
            tanner.render(e);
        }
        if (isChoc) {
            chocolate.render(e);
        }
    }

    public int timesTanned = 0;

    public void resetRender() {
        runtime.reset();
        runtime = StopWatch.start();
        startC = gainedC;
        startTime = System.currentTimeMillis();
        lastTradeTime = null;
        timesTanned++;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static int randInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public void reloadLines() {
        if (defaultLines) {
            defaultLines();
        } else {
            convertInputLines(inputLines);
        }
    }

    public void defaultLines() {
        linesArr = new String[]{"Can someone pls double my " + gp.getSgp() + " coins?",
                "I only have " + gp.getSgp() + " can anyone double it pls?",
                "I have " + gp.getSgp() + " Can someone double it so I buy pick",
                "Can Any1 Double My " + gp.getSgp() + " gp pls??",
                "Can someone Doble my coins pls :)",
                "Any1 willing to double " + gp.getSgp() + "?",
                "Can someone help a noob out and double my " + gp.getSgp() + "? :)",
                "Please someone double me so I can buy a pick and diggy diggy hole!",
                "Im newb can some1 help me and double " + gp.getSgp() + "! :)"};

        lines = new Lines(linesArr);
    }

    public void convertInputLines(String inputLines) {
        // Trims leading/trailing whitespace
        String inLines = inputLines.trim();

        // ENTER delimiter
        List<String> arr = Arrays.asList(inLines.split(System.lineSeparator()));
        linesArr = new String[arr.size()];
        int index = 0;

        // Checks for no gp amount in line
        for (String s : arr) {
            if (!s.contains("$")) {
                linesArr[index] = s;
                index++;
            }
        }

        List<String> arr2 = new ArrayList<>();
        for (String s2 : arr) {
            if (s2.contains("$")) {
                String[] temp = s2.split("\\$", -1);
                arr2.add(temp[0]);
                arr2.add(temp[1]);
            }
        }

        for (int i = 0; i < arr2.size(); i += 2) {

            // Checks for gp amount at start of line
            if (arr2.get(i) == null || arr2.get(i).equals("") || arr2.get(i).equals(" ")) {
                linesArr[index] = gp.getSgp() + arr2.get(i + 1);
                index++;
            }

            // Checks for gp amount at end of line
            else if (arr2.get(i + 1) == null || arr2.get(i + 1).equals("") || arr2.get(i + 1).equals(" ")) {
                linesArr[index] = arr2.get(i) + gp.getSgp();
                index++;
            }

            // Otherwise somewhere in middle
            else {
                linesArr[index] = arr2.get(i) + gp.getSgp() + arr2.get(i + 1);
                index++;
            }
        }

        lines = new Lines(linesArr);
    }

    public void checkWorldHopTime() {
        long currTime = System.currentTimeMillis();
        int elapsedSeconds = (int) ((currTime - startTime) / 1000);
        if (elapsedSeconds > (hopTime * 60) && !isMuling) {
            currWorld = Worlds.getCurrent();
            loadPopWorldsArr(5);
            hopTimeExpired = true;
            startTime = System.currentTimeMillis();
        }
    }

    public void removeCurrBegWorld(int currentWorld) {
        BufferedReader reader;
        try {
            File inputFile = new File(CURR_WORLD_PATH);
            File tempFile = new File(Script.getDataDirectory() + "\\TEMPCurrBegWorld.txt");

            if (!inputFile.exists())
                return;

            reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = Integer.toString(currentWorld);
            String currentLine;

            boolean rm = false;
            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.contains(lineToRemove) && !rm) {
                    rm = true;
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();

            if (inputFile.exists() && !inputFile.delete()) {
                Log.severe("Could not delete file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld(currentWorld);
            }

            if (tempFile.exists() && !tempFile.renameTo(inputFile)) {
                Log.severe("Could not rename file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld(currentWorld);
            }

        } catch (Exception e) {
            writeToErrorFile("Exception: removeCurrBegWorld(" + currentWorld + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void writeWorldToFile(int currentWorld) {
        File file = new File(CURR_WORLD_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(currentWorld);
        } catch (IOException e) {
            Log.severe("File not found");
            writeToErrorFile("FNF: writeWorldToFile()");
        }
    }

    public void writeToErrorFile(String errMsg) {
        try (FileWriter fw = new FileWriter(new File(ERROR_FILE_PATH), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(errMsg);
        } catch (IOException e) {
            Log.severe("Error file not found");
        }
    }

    private int leatherPrice = 0;
    private int cowhidePrice = 0;

    public int getTannerPPH(int tannedPH, boolean refreshPrices) {
        setTannerPrices(refreshPrices);
        setHighestProfitLeather();

        return tannedPH * (leatherPrice - cowhidePrice);
    }

    private void setTannerPrices(boolean refresh) {
        {
            int COWHIDE = 1739;
            try {
                leatherPrice = ExPriceChecker.getOSBuddySellPrice(LEATHER, refresh);
                cowhidePrice = ExPriceChecker.getOSBuddyBuyPrice(COWHIDE, refresh);
            } catch (IOException e) {
                Log.severe("Failed getting OSBuddy price");
                e.printStackTrace();
            } finally {
                try {
                    if (leatherPrice < 70) {
                        leatherPrice = ExPriceChecker.getRSBuddySellPrice(LEATHER, refresh);
                    }
                    if (cowhidePrice < 50) {
                        cowhidePrice = ExPriceChecker.getRSBuddyBuyPrice(COWHIDE, refresh);
                        //Log.fine("Using RSBuddy prices");
                    }
                } catch (IOException e) {
                    Log.severe("Failed getting RSBuddy price");
                    e.printStackTrace();
                } finally {
                    //Fall-back prices
                    if (leatherPrice < 70) {
                        if (lastPrices != null && lastPrices[0] >= 70) {
                            Log.fine("Using previous leather price");
                            leatherPrice = lastPrices[0];
                        } else {
                            Log.severe("Using fall-back leather price");
                            leatherPrice = 70;
                        }
                    } else {
                        if (leatherPrice != 70)
                            lastPrices[0] = leatherPrice;
                    }
                    if (cowhidePrice < 50) {
                        if (lastPrices != null && lastPrices[1] >= 50) {
                            Log.fine("Using previous cowhide price");
                            cowhidePrice = lastPrices[1];
                        } else {
                            Log.severe("Using fall-back cowhide price");
                            cowhidePrice = 170;
                        }
                    } else {
                        if (cowhidePrice != 170)
                            lastPrices[1] = cowhidePrice;
                    }
                }
            }
        }
    }

    private int LEATHER = 1741;

    public void setHighestProfitLeather() {
        int currLeather = LEATHER;
        int currProfit = (LEATHER == 1741) ? leatherPrice - cowhidePrice : leatherPrice - cowhidePrice - 2;

        // switch to other leather
        if (LEATHER == 1741) {
            LEATHER = 1743;
        } else {
            LEATHER = 1741;
        }

        setTannerPrices(false);
        if ((LEATHER == 1741 && (leatherPrice - cowhidePrice) < currProfit) ||
                (LEATHER == 1743 && (leatherPrice - cowhidePrice) - 2 < currProfit)) {
            LEATHER = currLeather;
            setTannerPrices(false);
        }
    }

    private int buyPrice = 0;
    private int sellPrice = 0;
    private static final int SELL_PL = 50;
    private static final int SET_SELL_PL = 70;
    private static final int BUY_PL = 30;
    private static final int SET_BUY_PL = 60;

    private void setChocolatePrices(boolean refresh) {
        try {
            sellPrice = ExPriceChecker.getOSBuddySellPrice(script.chocolate.Main.DUST, refresh);
            buyPrice = ExPriceChecker.getOSBuddyBuyPrice(script.chocolate.Main.BAR, refresh);
        } catch (IOException e) {
            Log.severe("Failed getting OSBuddy price");
            e.printStackTrace();
        } finally {
            try {
                if (sellPrice < SELL_PL) {
                    sellPrice = ExPriceChecker.getRSBuddySellPrice(script.chocolate.Main.DUST, refresh);
                }
                if (buyPrice < BUY_PL) {
                    buyPrice = ExPriceChecker.getRSBuddyBuyPrice(script.chocolate.Main.BAR, refresh);
                }
            } catch (IOException e) {
                Log.severe("Failed getting RSBuddy price");
                e.printStackTrace();
            } finally {
                fallbackPriceHelper();
            }
        }
        /*sellPrice -= subSellPrice;
        buyPrice += addBuyPrice;*/
    }

    private void fallbackPriceHelper() {
        //Fall-back prices
        if (sellPrice < SELL_PL) {
            if (lastPrices != null && lastPrices[2] >= SELL_PL) {
                Log.fine("Using previous sell price");
                sellPrice = lastPrices[2];
            } else {
                Log.severe("Using fall-back sell price");
                sellPrice = SET_SELL_PL;
            }
        } else {
            if (sellPrice != SET_SELL_PL) {
                ///Log.info("Sell price set to: " + sellPrice);
                lastPrices[2] = sellPrice;
            }
        }

        if (buyPrice < BUY_PL) {
            if (lastPrices != null && lastPrices[3] >= BUY_PL) {
                Log.fine("Using previous buy price");
                buyPrice = lastPrices[3];
            } else {
                Log.severe("Using fall-back buy price");
                buyPrice = SET_BUY_PL;
            }
        } else {
            if (buyPrice != SET_BUY_PL) {
                //Log.info("Buy price set to: " + buyPrice);
                lastPrices[3] = buyPrice;
            }
        }
    }

    public int getChocolatePPH(int grindPH, boolean refreshPrices) {
        setChocolatePrices(refreshPrices);
        return grindPH * (sellPrice - buyPrice);
    }

    public void loadPopWorldsArr(int loadTries) {
        int[] popWorldsArr = new int[3];
        RSWorld[] worlds = null;
        int maxW1 = 0;
        int maxW2 = 0;
        int maxW3 = 0;
        int maxP = 0;

        while (loadTries > 0 && (worlds == null || worlds.length < 3)) {
            worlds = Worlds.getLoaded(x -> x != null && x.getPopulation() >= minPop &&
                    !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            loadTries --;
            Time.sleep(500);
        }

        if (worlds == null || worlds.length < 3) {
            Log.severe("Using Default Worlds");
            writeToErrorFile("Using Default Worlds");
            return;
        }

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP) {
                maxW1 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[0] = maxW1;
        maxP = 0;

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP && w.getId() != maxW1) {
                maxW2 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[1] = maxW2;
        maxP = 0;

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP && w.getId() != maxW1 && w.getId() != maxW2) {
                maxW3 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[2] = maxW3;

        this.popWorldsArr = popWorldsArr;
    }

    public void setRandMuleKeep(int min, int max) {
        muleKeep = randInt(min, max);
        muleAmnt = (muleKeep + 100000);
    }
}
