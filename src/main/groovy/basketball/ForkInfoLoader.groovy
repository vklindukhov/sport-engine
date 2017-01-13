package basketball

import football2.MarathonForkScanner

import java.time.LocalDate

import static java.time.format.DateTimeFormatter.ofPattern

path = '/todayTest.html'
def scanningMillisecondsInterval = 60_000

println('TODAY IS ' + LocalDate.now().getDayOfWeek() + ' ' + LocalDate.now().format(ofPattern('dd/MM/yyyy')))
Timer timer = new Timer();
timer.schedule(new MarathonForkScanner(), 0, scanningMillisecondsInterval);

