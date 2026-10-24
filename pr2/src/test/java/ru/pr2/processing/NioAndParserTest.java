package ru.pr2.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.pr2.model.SupplierProduct;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NioAndParserTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void readsUtf8UsingFileChannelAndByteBuffer() throws Exception {
        Path file = temporaryDirectory.resolve("text.txt");
        String expected = "Первая строка\nВторая строка — UTF-8\n";
        Files.writeString(file, expected, StandardCharsets.UTF_8);

        assertEquals(expected, new NioTextReader().read(file));
    }

    @Test
    void parsesQuotedCsvField() throws Exception {
        Path file = temporaryDirectory.resolve("supplier.csv");
        Files.writeString(file, """
                supplierId,supplierName,product,price,quantity
                10,"Компания, ООО",Кабель,199.90,8
                """, StandardCharsets.UTF_8);

        List<SupplierProduct> products = new CsvSupplierParser(new NioTextReader()).parse(file);

        assertEquals(1, products.size());
        assertEquals("Компания, ООО", products.getFirst().supplierName());
        assertEquals("199.90", products.getFirst().price().toPlainString());
    }

    @Test
    void parsesJsonArray() throws Exception {
        Path file = temporaryDirectory.resolve("supplier.json");
        Files.writeString(file, """
                [{
                  "supplierId": 11,
                  "supplierName": "JSON-поставщик",
                  "product": "Маршрутизатор",
                  "price": 4500.00,
                  "quantity": 3
                }]
                """, StandardCharsets.UTF_8);

        List<SupplierProduct> products = new JsonSupplierParser(
                new NioTextReader(), new ObjectMapper()).parse(file);

        assertEquals(1, products.size());
        assertEquals("Маршрутизатор", products.getFirst().product());
    }
}
