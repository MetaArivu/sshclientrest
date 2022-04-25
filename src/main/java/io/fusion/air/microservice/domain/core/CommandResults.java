package io.fusion.air.microservice.domain.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;

/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public final class CommandResults {

    private final String command;
    private final String result;
    private final Date startTime;
    private final long elapsedTime;

    /**
     * Create Command Results
     *
     * @param _cmd
     * @param _result
     * @param _startTime
     */
    public CommandResults(String _cmd, String _result, Date _startTime) {
        command     = _cmd;
        result      = _result;
        startTime   = _startTime;
        long endTime= System.currentTimeMillis();
        elapsedTime = endTime - startTime.getTime();
    }

    /**
     * Returns the Command
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the Result
     * @return
     */
    public String getResult() {
        return result;
    }

    /**
     * Returns the Start Time
     * @return
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Returns the Elapsed Time
     * @return
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        String isoDt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(startTime);
        sb.append("{");
        sb.append("\"date\": \"").append(isoDt).append("\",");
        sb.append("\"command\": \"").append(command).append("\",");
        // sb.append("\"result\": \"").append(result.replaceAll("[\\r\\n]+", "[newline]")).append("\",");
        // “\R” pattern instead of “\\r?\\n|\\r” in Java 8 or higher.
        String[] lines = result.split("\\R");
        sb.append("\"result\":[");
        int x=1;
        for(String line : lines) {
            sb.append("\"").append(line).append("\"");
            if(x < lines.length) {
                sb.append(",");
            }
            x++;
        }
        sb.append("],");
        sb.append("\"timeTaken\": ").append(elapsedTime);
        sb.append("}");
        return sb.toString();
    }
}
