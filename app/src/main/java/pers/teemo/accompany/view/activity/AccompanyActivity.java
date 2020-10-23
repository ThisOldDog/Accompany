package pers.teemo.accompany.view.activity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xkzhangsan.time.LunarDate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pers.teemo.accompany.R;
import pers.teemo.accompany.asset.AssetLoader;
import pers.teemo.accompany.entity.Companion;
import pers.teemo.accompany.entity.Event;
import pers.teemo.accompany.view.adapter.SimpleBaseAdapter;

public class AccompanyActivity extends AppCompatActivity {
    private static final long PERIOD = 1000 * 60 * 60 * 24;
    private final Timer TIMER = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accompany);
        // 总时间
        Companion companion = AssetLoader.getInstance(getApplicationContext().getAssets(), Companion.class);
        TotalDateTimeUpdater totalDateTimeUpdater = new TotalDateTimeUpdater(
                companion,
                findViewById(R.id.text_view_total_date_time),
                getResources().getString(R.string.accompany_total_date_time_full));
        runOnUiThread(totalDateTimeUpdater);
        // 事件列表
        ListView listViewEvent = findViewById(R.id.list_view_event);
        SimpleBaseAdapter<Event> eventSimpleBaseAdapter = new SimpleBaseAdapter<>(
                R.layout.activity_accompany_list_event,
                this,
                getEventList(companion),
                ((adapter, position, convertView, parent) -> {
                    Event event = adapter.getDataList().get(position);
                    ((TextView) convertView.findViewById(R.id.text_view_event_month_day))
                            .setText(String.format(
                                    Locale.getDefault(),
                                    getResources().getString(R.string.accompany_total_list_event_month_day),
                                    event.getStartDate().getMonthValue(), event.getStartDate().getDayOfMonth()));
                    ((TextView) convertView.findViewById(R.id.text_view_event_year))
                            .setText(String.format(
                                    Locale.getDefault(),
                                    getResources().getString(R.string.accompany_total_list_event_year),
                                    event.getStartDate().getYear()));
                    ((TextView) convertView.findViewById(R.id.text_view_event_days))
                            .setText(String.format(
                                    Locale.getDefault(),
                                    getResources().getString(R.string.accompany_total_list_event_days),
                                    ChronoUnit.DAYS.between(LocalDate.now(), event.getStartDate())));
                    ((TextView) convertView.findViewById(R.id.text_view_event_name))
                            .setText(event.getEventName());
                }));
        listViewEvent.setAdapter(eventSimpleBaseAdapter);
        eventSimpleBaseAdapter.notifyDataSetChanged();
        TIMER.scheduleAtFixedRate(totalDateTimeUpdater,
                Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atTime(0, 0)).toMillis(),
                PERIOD);
    }

    private List<Event> getEventList(Companion companion) {
        LocalDate nextBirthday = companion.getBirthdayDate().withYear(LocalDate.now().getYear());
        nextBirthday = nextBirthday.isBefore(LocalDate.now()) ? nextBirthday.plusYears(1) : nextBirthday;
        Event birthdayEvent = new Event().setEventName("阳历生日").setStartDate(nextBirthday);

        LunarDate lunarBirthday = LunarDate.from(companion.getBirthdayDate());
        LunarDate nextLunarBirthday = LunarDate.from(LocalDate.now());
        while (nextLunarBirthday.getlMonth() != lunarBirthday.getlMonth() || nextLunarBirthday.getlDay() != lunarBirthday.getlDay()) {
            nextLunarBirthday = LunarDate.from(nextLunarBirthday.getLocalDate().plusDays(1));
        }
        Event lunarBirthdayEvent = new Event().setEventName("阴历生日").setStartDate(nextLunarBirthday.getLocalDate());
        if (birthdayEvent.getStartDate().isBefore(lunarBirthdayEvent.getStartDate())) {
            return Arrays.asList(birthdayEvent, lunarBirthdayEvent);
        } else {
            return Arrays.asList(lunarBirthdayEvent, birthdayEvent);
        }
    }

    private static class TotalDateTimeUpdater extends TimerTask {
        private final Companion companion;
        private final TextView textViewTotalDateTime;
        private final String format;

        public TotalDateTimeUpdater(Companion companion, TextView textViewTotalDateTime, String format) {
            this.companion = companion;
            this.textViewTotalDateTime = textViewTotalDateTime;
            this.format = format;
        }

        @Override
        public void run() {
            Period between = Period.between(companion.getStartDate(), LocalDate.now());
            textViewTotalDateTime.setText(String.format(Locale.getDefault(), format, between.getYears(), between.getMonths(), between.getDays()));
        }
    }
}