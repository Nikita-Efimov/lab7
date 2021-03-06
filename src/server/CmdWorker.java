import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Key {
    public Function<String, String> func;
    public String description;

    public Key(Function<String, String> func, String description) {
        this.func = func;
        this.description = description;
    }
}

public class CmdWorker {
    protected Map<String, Key> cmdMap;
    protected Date initDate;
    protected Date lastChangeDate;
    protected final String CMD_NOT_FOUND = "command not found";
    protected final String VOID_STR = "void";

    {
        initDate = new Date();
        lastChangeDate = initDate;
        cmdMap = new HashMap<>();

        // cmdMap.put("load", new Key(this::load,  ""));
        // cmdMap.put("save", new Key(this::save,  ""));
        cmdMap.put("add", new Key(this::add,  "добавить новый элемент в коллекцию"));
        cmdMap.put("remove_first", new Key(this::removeFirst,  "удалить первый элемент из коллекции"));
        cmdMap.put("show", new Key(this::show,  "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"));
        cmdMap.put("info", new Key(this::info,  "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)"));
        cmdMap.put("remove_lower", new Key(this::removeLower,  "удалить из коллекции все элементы, меньшие, чем заданный"));
        cmdMap.put("remove", new Key(this::remove,  "удалить элемент из коллекции по его значению"));
        cmdMap.put("add_if_max", new Key(this::addIfMax,  "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"));
        cmdMap.put("list", new Key(this::list,  "просмотр списка команд"));
        cmdMap.put("help", new Key(this::list,  "просмотр списка команд"));
        cmdMap.put("man", new Key(this::man,  "описание команд"));
    }

    public CmdWorker() {}

    public String doCmd(String cmd) {
        int indexOfSpace = cmd.indexOf(' ');
        indexOfSpace = indexOfSpace == -1 ? cmd.length() : indexOfSpace;
        String[] splittedCmd = {cmd.substring(0, indexOfSpace), indexOfSpace != cmd.length() ? cmd.substring(indexOfSpace + 1, cmd.length()).trim() : VOID_STR};

        String out = "";
        try {
            out = cmdMap.get(splittedCmd[0]).func.apply(splittedCmd[1]);
        } catch (NullPointerException e) {
            out = CMD_NOT_FOUND;
        } catch(Exception e) {
            out = "uncorrect command syntax";
        }

        return out;
    }

    protected City processInput(String jsonInput) {
        City city = null;
        String name;
        Integer size, x, y;

        if (jsonInput.indexOf('}') < jsonInput.length() - 1)
            throw new RuntimeException("uncorrect command");

        try {
            JSONObject obj = new JSONObject(jsonInput);

            try {
                name = obj.getString("name");
            } catch (JSONException e) {
                name = "Unnamed";
            }

            try {
                size = obj.getInt("size");
            } catch (JSONException e) {
                size = 0;
            }

            try {
                x = obj.getInt("x");
            } catch (JSONException e) {
                x = 0;
            }

            try {
                y = obj.getInt("y");
            } catch (JSONException e) {
                y = 0;
            }

            city = new City(name, size, x, y);

        } catch (JSONException e) {
            throw new RuntimeException("uncorrect command");
        }

        return city;
    }

    public void set(PriorityQueue<City> pq) {
        DBCityCollection db = (DBCityCollection)Server.db;

        for (City city : pq) {
            db.add(city);
        }
    }

    public String load(final String filename) {
        try {
            FileReader reader = new FileReader(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(reader));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("city");

            DBCityCollection db = (DBCityCollection)Server.db;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element)nNode;

                    City city = new City(eElement.getElementsByTagName("name").item(0).getTextContent(),
                             Integer.parseInt(eElement.getElementsByTagName("size").item(0).getTextContent()),
                             Integer.parseInt(eElement.getElementsByTagName("x").item(0).getTextContent()),
                             Integer.parseInt(eElement.getElementsByTagName("y").item(0).getTextContent()),
                             Integer.parseInt(eElement.getElementsByTagName("init-date").item(0).getTextContent()));

                    db.add(city);
                }
            }
        } catch (java.io.FileNotFoundException e) {
            return "file not found";
        } catch (Exception e) {
            return "error while parsing";
        }

        return "";
    }

    public String save(final String filename) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element cityCont = document.createElement("city-cont");
            document.appendChild(cityCont);

            Element city, name, size, x, y, initDate;
            PriorityQueue<City> priorityQueue = new PriorityQueue();
            for (City elem : priorityQueue) {
                city = document.createElement("city");
                cityCont.appendChild(city);

                name = document.createElement("name");
                name.appendChild(document.createTextNode(elem.name));
                city.appendChild(name);

                size = document.createElement("size");
                size.appendChild(document.createTextNode(elem.areaSize.toString()));
                city.appendChild(size);

                x = document.createElement("x");
                x.appendChild(document.createTextNode(elem.x.toString()));
                city.appendChild(x);

                y = document.createElement("y");
                y.appendChild(document.createTextNode(elem.y.toString()));
                city.appendChild(y);

                initDate = document.createElement("init-date");
                initDate.appendChild(document.createTextNode(((Integer)(int)elem.getInitDate()).toString()));
                city.appendChild(initDate);
            }

            DOMSource domSource = new DOMSource(document);
            StreamResult result = new StreamResult(new BufferedWriter(new FileWriter(filename)));

            // create the xml file
            // transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String removeLower(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return "";

        DBCityCollection db = (DBCityCollection)Server.db;
        if (db.removeLower(city))
            lastChangeDate = new Date();

        return "";
    }

    public String addIfMax(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return "";

        DBCityCollection db = (DBCityCollection)Server.db;
        if (db.addIfMax(city))
            lastChangeDate = new Date();

        return "";
    }

    public String remove(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return "";

        DBCityCollection db = (DBCityCollection)Server.db;
        db.remove(city);

        return "";
    }

    public String add(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return "";

        DBCityCollection db = (DBCityCollection)Server.db;
        db.add(city);

        lastChangeDate = new Date();
        return "";
    }

    public String info(String str) {
        String out = "Дата последнего изменения: " + (String)lastChangeDate.toLocaleString() + '\n';
        return out;
    }

    public String show(String ignore) {
        DBCityCollection db = (DBCityCollection)Server.db;
        return db.show();
    }

    public String removeFirst(String ignore) {
        DBCityCollection db = (DBCityCollection)Server.db;
        db.removeFirst();

        lastChangeDate = new Date();
        return "";
    }

    public String man(String cmd) {
        String out = "";
        try {
            out = (String)cmdMap.get(cmd).description;
        } catch (NullPointerException e) {
            out = "right syntax is man <command>";
        }
        return out;
    }

    public String list(String str) {
        return cmdMap
        .keySet()
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining("\n"));
    }
}
