
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLstaxParser {
    public static void main(String[] args) {
        File file = new File(Objects.requireNonNull(XMLstaxParser.class.getClassLoader().getResource("example.xml")).getFile());
        List<Issue> issueList = parseXMLFile(file);
        for (Issue issue: issueList) {
            System.out.println(issue.id);
        }
    }

    public static List<Issue> parseXMLFile(File file) {
        List<Issue> issuesList = new ArrayList<>();
        Issue issue = null;
        AssignedTo assignedTo = null;
        Author author = null;
        Status status = null;
        SrsBlock srsBlock = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            // инициализируем reader и скармливаем ему xml файл
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(file));
            // проходим по всем элементам xml файла
            while (reader.hasNext()) {
                // получаем событие (элемент) и разбираем его по атрибутам
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("Issue")) {
                        issue = new Issue();
                        // Получаем атрибут id для каждого элемента Issue
                        Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                        if (idAttr != null) {
                            issue.id = (Integer.parseInt(idAttr.getValue()));
                        }
                    } else if (startElement.getName().getLocalPart().equals("name")) {
                        xmlEvent = reader.nextEvent();
                        issue.name = (xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("description")) {
                        xmlEvent = reader.nextEvent();
                        issue.description = (xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("created_at")) {
                        xmlEvent = reader.nextEvent();
                        issue.created_at = formatter.parse(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("start_at")) {
                        xmlEvent = reader.nextEvent();
                        issue.start_at = formatter.parse(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("due_date")) {
                        xmlEvent = reader.nextEvent();
                        issue.due_date = formatter.parse(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("priority")) {
                        xmlEvent = reader.nextEvent();
                        issue.priority = Integer.parseInt(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("parent")) {
                        xmlEvent = reader.nextEvent();
                        issue.parent = Integer.parseInt(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("assigned_to")) {
                        assignedTo = new AssignedTo();
                        StartElement assignedToElem = startElement;
                        while (true) {
                            xmlEvent = reader.nextEvent();
                            xmlEvent = reader.nextEvent();
                            xmlEvent = reader.nextEvent();
                            xmlEvent = reader.nextEvent();
                            System.out.println(xmlEvent.asCharacters().getData());
                            xmlEvent = reader.nextEvent();
                            System.out.println(xmlEvent.asCharacters().getData());
                            if (startElement.isStartElement()) {
                                break;
                            }
                        }
                        if (startElement.getName().getLocalPart().equals("name")) {
                            System.out.println(xmlEvent.asCharacters().getData());
                        }
//                        StartElement assignedElem = xmlEvent.asStartElement();
//                        if (startElement.getName().getLocalPart().equals("assigned_to")) {
//                            assignedTo = new AssignedTo();
//                            // Получаем атрибут id для каждого элемента Issue
//                            if (assignedElem.getName().getLocalPart().equals("name")) {
//                                xmlEvent = reader.nextEvent();
//                                assignedTo.name = (xmlEvent.asCharacters().getData());
//                                System.out.println(assignedTo.name);
//                            }
//                        }
                    }
                }
                // если цикл дошел до закрывающего элемента Issue,
                // то добавляем считанного из файла студента в список
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("Issue")) {
                        issuesList.add(issue);
                    }
                }
            }

        } catch (FileNotFoundException | XMLStreamException exc) {
            exc.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return issuesList;
    }
}
