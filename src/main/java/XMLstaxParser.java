
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
        User user = null;
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
                    } else if (startElement.getName().getLocalPart().equals("status")) {
                        xmlEvent = reader.nextEvent();
                        issue.status = xmlEvent.asCharacters().getData();
                    } else if (startElement.getName().getLocalPart().equals("priority")) {
                        xmlEvent = reader.nextEvent();
                        issue.priority = Integer.parseInt(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("parent")) {
                        xmlEvent = reader.nextEvent();
                        issue.parent = Integer.parseInt(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("assigned_to")) {
                        issue.assigned_to = parseAssigned(reader);
                    } else if (startElement.getName().getLocalPart().equals("author")) {
                        issue.author = parseAssigned(reader);
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

    public static User parseAssigned(XMLEventReader reader) throws XMLStreamException {
        User user = new User();
        StartElement assignedToElem = null;
        while (true) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.getEventType() != 1) {
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("assigned_to") || endElement.getName().getLocalPart().equals("author")) {
                        break;
                    }
                }
                continue;
            }
            assignedToElem = xmlEvent.asStartElement();
            switch (xmlEvent.getEventType()) {
                case 1:
                    if (assignedToElem.getName().getLocalPart().equals("name")) {
                        xmlEvent = reader.nextEvent();
                        user.name = xmlEvent.asCharacters().getData();
                    } else if (assignedToElem.getName().getLocalPart().equals("lastname")) {
                        xmlEvent = reader.nextEvent();
                        user.lastname = xmlEvent.asCharacters().getData();
                    } else if (assignedToElem.getName().getLocalPart().equals("middlename")) {
                        xmlEvent = reader.nextEvent();
                        user.middlename = xmlEvent.asCharacters().getData();
                    } else if (assignedToElem.getName().getLocalPart().equals("email")) {
                        xmlEvent = reader.nextEvent();
                        user.email = xmlEvent.asCharacters().getData();
                    } else if (assignedToElem.getName().getLocalPart().equals("admin")) {
                        xmlEvent = reader.nextEvent();
                        user.admin = Boolean.parseBoolean(xmlEvent.asCharacters().getData());
                    }
                    break;
                default:
                    continue;
            }
        }
        return user;
    }
}
