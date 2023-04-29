package com.centralhardware.telegram.interactiveBookBot.engine.clickhouse;

import com.centralhardware.telegram.interactiveBookBot.engine.clickhouse.model.LogEntry;
import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseDataStreamFactory;
import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.data.ClickHousePipedOutputStream;
import com.clickhouse.data.format.BinaryStreamUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class Clickhouse {

    private final ClickHouseNode server;

    public Clickhouse(ClickHouseNode server){
        this.server = server;

        createTable();
    }

    private void createTable(){
        try (ClickHouseClient client = openConnection()){
            ClickHouseRequest<?> request = client.read(server);
            request.query("""
                    CREATE TABLE IF NOT EXISTS interactive_bot_statistic
                    (
                        date_time DateTime,
                        chat_id BIGINT,
                        username Nullable(String),
                        first_name Nullable(String),
                        last_name Nullable(String),
                        is_premium bool,
                        action String,
                        text VARCHAR(256),
                        bookId Nullable(UUID),
                        partId Nullable(BIGINT),
                        authorId Nullable(UUID),
                    )
                    engine = MergeTree
                    ORDER BY date_time
                    """)
                    .execute().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(LogEntry entry){
        try (ClickHouseClient client = openConnection()){
            ClickHouseRequest.Mutation request =  client
                    .read(server)
                    .write()
                    .table("interactive_bot_statistic")
                    .format(ClickHouseFormat.RowBinary);

            ClickHouseConfig config = request.getConfig();
            CompletableFuture<ClickHouseResponse> future;

            try (ClickHousePipedOutputStream stream = ClickHouseDataStreamFactory.getInstance()
                    .createPipedOutputStream(config, (Runnable) null)){
                future = request.data(stream.getInputStream()).execute();
                write(stream, entry.dateTime());
                write(stream,entry.chatId());
                writeNullable(stream, entry.username());
                writeNullable(stream, entry.firstName());
                writeNullable(stream, entry.lastName());
                write(stream, entry.isPremium() != null && entry.isPremium());
                write(stream, entry.action());
                write(stream,entry.text());
                writeNullable(stream, entry.bookId());
                writeNullable(stream, entry.partId());
                writeNullable(stream, entry.authorId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (ClickHouseResponse response = future.get()){
                log.info("{} row inserted in clickhouse", response.getSummary().getWrittenRows());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void writeNullable(OutputStream stream, Object value) throws IOException {
        if (value == null){
            BinaryStreamUtils.writeNull(stream);
            return;
        }
        BinaryStreamUtils.writeNonNull(stream);
        write(stream, value);
    }

    private void write(OutputStream stream, Object value) throws IOException {
        if (value instanceof String string){
            BinaryStreamUtils.writeString(stream, string);
        } else if (value instanceof UUID uuid){
            BinaryStreamUtils.writeUuid(stream, uuid);
        } else if (value instanceof Integer integer){
            BinaryStreamUtils.writeInt64(stream, integer);
        } else if (value instanceof  Long bigint){
            BinaryStreamUtils.writeInt64(stream, bigint);
        } else if (value instanceof Boolean bool){
            BinaryStreamUtils.writeBoolean(stream, bool);
        } else if (value instanceof LocalDateTime dateTime){
            BinaryStreamUtils.writeDateTime(stream, dateTime, TimeZone.getDefault());
        }
    }

    private ClickHouseClient openConnection(){
        return ClickHouseClient.newInstance(server.getProtocol());
    }

}
