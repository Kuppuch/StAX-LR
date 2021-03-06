
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLstaxParser {
    public static void main(String[] args) throws TransformerConfigurationException, IOException, SAXException, XMLStreamException {
        boolean ok = schemaValidator();
        if (ok) {
            File file = new File(Objects.requireNonNull(XMLstaxParser.class.getClassLoader().getResource("example.xml")).getFile());
            List<Issue> issueList = parseXMLFile(file);
            for (Issue issue: issueList) {
                System.out.println(issue.id);
            }
            htmlGen(issueList);
        }
    }

    public static boolean schemaValidator() throws SAXException, IOException, XMLStreamException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(".\\src\\main\\resources\\issue.xsd"));
            Validator validator = schema.newValidator();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(Files.newInputStream(Paths.get(".\\src\\main\\resources\\example.xml")));
            validator.validate(new StAXSource(reader));

            System.out.println("XML is valid");
            return true;
        } catch (Exception e) {
            System.out.println("XML is not valid");
            return false;
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
            // ???????????????????????????? reader ?? ?????????????????????? ?????? xml ????????
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(file));
            // ???????????????? ???? ???????? ?????????????????? xml ??????????
            while (reader.hasNext()) {
                // ???????????????? ?????????????? (??????????????) ?? ?????????????????? ?????? ???? ??????????????????
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("Issue")) {
                        issue = new Issue();
                        // ???????????????? ?????????????? id ?????? ?????????????? ???????????????? Issue
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
                    } else if (startElement.getName().getLocalPart().equals("SrsBlock")) {
                        issue.srsBlock = parseSrsBlock(reader);
                    }
                }
                // ???????? ???????? ?????????? ???? ???????????????????????? ???????????????? Issue,
                // ???? ?????????????????? ???????????????????? ???? ?????????? ???????????????? ?? ????????????
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
            if (xmlEvent.getEventType() == 1) {
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
            } else {
                continue;
            }
        }
        return user;
    }

    public static SrsBlock parseSrsBlock(XMLEventReader reader) throws XMLStreamException {
        SrsBlock srsBlock = new SrsBlock();
        StartElement assignedToElem = null;
        int cntEnds = 0;
        while (true) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.getEventType() != 1) {
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("SrsBlock")) {
                        break;
                    }
                }
                continue;
            }
            assignedToElem = xmlEvent.asStartElement();
            if (xmlEvent.getEventType() == 1) {
                if (assignedToElem.getName().getLocalPart().equals("Title")) {
                    srsBlock.title = parseSrsBlockElem(reader);
                } else if (assignedToElem.getName().getLocalPart().equals("Content")) {
                    srsBlock.content = parseSrsBlockElem(reader);
                }
            } else {
                continue;
            }
        }
        return srsBlock;
    }

    public static SrsBlockElem parseSrsBlockElem(XMLEventReader reader) throws XMLStreamException {
        SrsBlockElem srsBlockElem = new SrsBlockElem();
        StartElement srsBlockElemXml = null;
        while (true) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.getEventType() != 1) {
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("Title") || endElement.getName().getLocalPart().equals("Content")) {
                        break;
                    }
                }
                continue;
            }
            srsBlockElemXml = xmlEvent.asStartElement();
            if (xmlEvent.getEventType() == 1) {
                if (srsBlockElemXml.getName().getLocalPart().equals("Value")) {
                    xmlEvent = reader.nextEvent();
                    srsBlockElem.value = xmlEvent.asCharacters().getData();
                } else if (srsBlockElemXml.getName().getLocalPart().equals("Property")) {
                    srsBlockElem.property = parseProperty(reader);
                }
            }
        }
        return srsBlockElem;
    }

    public static Property parseProperty (XMLEventReader reader) throws XMLStreamException {
        Property property = new Property();
        StartElement srsBlockElemXml = null;
        while (true) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.getEventType() != 1) {
                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("Property")) {
                        break;
                    }
                }
                continue;
            }
            srsBlockElemXml = xmlEvent.asStartElement();
            if (xmlEvent.getEventType() == 1) {
                if (srsBlockElemXml.getName().getLocalPart().equals("Value")) {
                    xmlEvent = reader.nextEvent();
                    property.value = Integer.parseInt(xmlEvent.asCharacters().getData());
                } else if (srsBlockElemXml.getName().getLocalPart().equals("Parameter")) {
                    xmlEvent = reader.nextEvent();
                    property.parameter = xmlEvent.asCharacters().getData();
                }
            }
        }
        return property;
    }

    public static void htmlGen(List<Issue> issueList) throws IOException, SAXException, TransformerConfigurationException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String encoding = "UTF-8";
        File file = new File(Objects.requireNonNull(XMLstaxParser.class.getClassLoader().getResource("example.xml")).getFile());
        FileOutputStream fos = new FileOutputStream(".\\myfile.html");
        OutputStreamWriter writer = new OutputStreamWriter(fos, encoding);
        StreamResult streamResult = new StreamResult(writer);

        SAXTransformerFactory saxFactory =
                (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler tHandler = saxFactory.newTransformerHandler();
        tHandler.setResult(streamResult);

        Transformer transformer = tHandler.getTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");


        writer.write("<!DOCTYPE html>\n");
        writer.flush();
        tHandler.startDocument();
        tHandler.startElement("", "", "html", new AttributesImpl());
        tHandler.startElement("", "", "head", new AttributesImpl());
        tHandler.startElement("", "", "link rel=\"stylesheet\" href=\"mysite.css\"", new AttributesImpl());
        tHandler.startElement("", "", "title", new AttributesImpl());
        tHandler.characters("Issue".toCharArray(), 0, 5);
        tHandler.endElement("", "", "title");
        tHandler.startElement("", "", "title", new AttributesImpl());
        tHandler.characters("Issue".toCharArray(), 0, 5);
        tHandler.endElement("", "", "title");
        tHandler.endElement("", "", "head");
        tHandler.startElement("", "", "body", new AttributesImpl());
        tHandler.startElement("", "", "p", new AttributesImpl());
        for (Issue i: issueList) {
            tHandler.characters(i.name.toCharArray(), 0, i.name.length());
            tHandler.characters(i.status.toCharArray(), 0, i.status.length());
            tHandler.characters(i.assigned_to.name.toCharArray(), 0, i.assigned_to.name.length());
            tHandler.characters(i.assigned_to.lastname.toCharArray(), 0, i.assigned_to.lastname.length());
            tHandler.startElement("", "", "br", new AttributesImpl());
        }
        tHandler.endElement("", "", "p");

        tHandler.startElement("", "", "table", new AttributesImpl());
                //???????????????? ????????????
                tHandler.startElement("", "", "td", new AttributesImpl());
                    tHandler.characters("???????????????? ????????????".toCharArray(), 0, 15);
                tHandler.endElement("", "", "td");
                //????????????
                tHandler.startElement("", "", "td", new AttributesImpl());
                    tHandler.characters("????????????".toCharArray(), 0, 6);
                tHandler.endElement("", "", "td");
                //??????????????????????
                tHandler.startElement("", "", "td", new AttributesImpl());
                    tHandler.characters("??????????????????????".toCharArray(), 0, 11);
                tHandler.endElement("", "", "td");
        //???????? ????????????
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("???????? ????????????".toCharArray(), 0, 11);
        tHandler.endElement("", "", "td");
        //???????? ????????????????????
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("???????? ????????????????????".toCharArray(), 0, "???????? ????????????????????".length());
        tHandler.endElement("", "", "td");
        //???????????????????? ???????? ???? ????????????????????
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("???????????????????? ???????? ???? ????????????????????".toCharArray(), 0, "???????????????????? ???????? ???? ????????????????????".length());
        tHandler.endElement("", "", "td");
        //????????????????????????????
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("????????????????????????????".toCharArray(), 0, "????????????????????????????".length());
        tHandler.endElement("", "", "td");


        for (Issue i: issueList) {
            tHandler.startElement("", "", "tr", new AttributesImpl());
            //???????????????? ????????????
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(i.name.toCharArray(), 0, i.name.length());
            tHandler.endElement("", "", "td");
            //????????????
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(i.status.toCharArray(), 0, i.status.length());
            tHandler.endElement("", "", "td");
            //??????????????????????
            tHandler.startElement("", "", "td", new AttributesImpl());
            String fullName = i.assigned_to.name + " " + i.assigned_to.lastname;
            tHandler.characters(fullName.toCharArray(), 0, fullName.length());
            tHandler.endElement("", "", "td");


            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(formatter.format(i.created_at).toCharArray(), 0, formatter.format(i.created_at).length());
            tHandler.endElement("", "", "td");

            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(formatter.format(i.due_date).toCharArray(), 0, formatter.format(i.due_date).length());
            tHandler.endElement("", "", "td");

            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(Long.toString((i.due_date.getTime()-i.created_at.getTime())/ (24 * 60 * 60 * 1000)).toCharArray(), 0, Long.toString((i.due_date.getTime()-i.created_at.getTime())/ (24 * 60 * 60 * 1000)).length());
            tHandler.endElement("", "", "td");

            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(Integer.toString(i.priority).toCharArray(), 0, Integer.toString(i.priority).length());
            tHandler.endElement("", "", "td");

            tHandler.endElement("", "", "tr");
        }
        tHandler.startElement("", "", "tr", new AttributesImpl());
        tHandler.startElement("", "", "th", new AttributesImpl());
        tHandler.endElement("", "", "th");
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("?????????? ??????????".toCharArray(), 0, 11);
        tHandler.endElement("", "", "td");
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters(Integer.toString(issueList.size()).toCharArray(), 0, Integer.toString(issueList.size()).length());
        tHandler.endElement("", "", "td");
        tHandler.endElement("", "", "tr");

        tHandler.endElement("", "", "table");

        tHandler.endElement("", "", "body");
        tHandler.endElement("", "", "html");
        tHandler.endDocument();
        writer.close();

        fos.close();
    }

}
