package pers.teemo.accompany.view.activity;

import android.content.SharedPreferences;
import android.content.res.Resources;
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
    private static final String SETTING = "ACCOMPANY_SETTING";
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
                getResources(),
                getSharedPreferences(SETTING, MODE_PRIVATE));
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
                                    getResources().getString(R.string.date_month_day),
                                    event.getStartDate().getMonthValue(), event.getStartDate().getDayOfMonth()));
                    ((TextView) convertView.findViewById(R.id.text_view_event_year))
                            .setText(String.format(
                                    Locale.getDefault(),
                                    getResources().getString(R.string.date_year),
                                    event.getStartDate().getYear()));
                    ((TextView) convertView.findViewById(R.id.text_view_event_days))
                            .setText(String.format(
                                    Locale.getDefault(),
                                    getResources().getString(R.string.date_day),
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
        public static final String DISPLAY = "DISPLAY";
        private final Companion companion;
        private final TextView textViewTotalDateTime;
        private final Resources resources;
        private final SharedPreferences sharedPreferences;

        public TotalDateTimeUpdater(Companion companion, TextView textViewTotalDateTime, Resources resources, SharedPreferences sharedPreferences) {
            this.companion = companion;
            this.textViewTotalDateTime = textViewTotalDateTime;
            this.resources = resources;
            this.sharedPreferences = sharedPreferences;
            setAction();
        }

        public void setAction() {
            textViewTotalDateTime.setOnClickListener(v -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TotalDateTimeUpdater.DISPLAY,
                        TotalNameDisplayMode.next(sharedPreferences.getString(TotalDateTimeUpdater.DISPLAY, TotalNameDisplayMode.DAY.name())));
                editor.apply();
                editor.commit();
                refreshDisplay();
            });
        }

        @Override
        public void run() {
            refreshDisplay();
        }

        public void refreshDisplay() {
            textViewTotalDateTime.setText(getDisplay());
        }

        private String getDisplay() {

            switch (TotalNameDisplayMode.valueOf(sharedPreferences.getString(DISPLAY, TotalNameDisplayMode.DATE.name()))) {
                case DAY:
                    return String.format(Locale.getDefault(), resources.getString(R.string.date_day), companion.getStartDate().until(LocalDate.now(), ChronoUnit.DAYS));
                case DATE:
                default:
                    Period between = Period.between(companion.getStartDate(), LocalDate.now());
                    int year, month;
                    if ((year = between.getYears()) > 0) {
                        return String.format(Locale.getDefault(), resources.getString(R.string.date_year_month_day), year, between.getMonths(), between.getDays());
                    } else if ((month = between.getMonths()) > 0) {
                        return String.format(Locale.getDefault(), resources.getString(R.string.date_month_day), month, between.getDays());
                    } else {
                        return String.format(Locale.getDefault(), resources.getString(R.string.date_day), between.getDays());
                    }
            }
        }
    }

    private enum TotalNameDisplayMode {
        DAY,
        DATE;

        public static String next(String mode) {
            if (DAY.name().equals(mode)) {
                return DATE.name();
            } else {
                return DAY.name();
            }
        }
    }
}