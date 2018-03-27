package jacobfix.scoreprog;

import jacobfix.scoreprog.schedule.ScheduleRetriever;

public class RetrieverFactory {

    // private DetailsRetriever detailsRetriever = new DetailsRetriever();
    private ScheduleRetriever scheduleRetriever = new ScheduleRetriever();

    public ScheduleRetriever getScheduleRetriever() {
        return scheduleRetriever;
    }
}
