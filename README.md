# Эмулятор микрокалькулятора МК-52

![JDK](docs/java-24.svg)
[![License](docs/license.svg)](LICENSE)

![МК-52](docs/main-window.png)

Проект носит исключительно развлекательный характер и не преследует цели точно воспроизвести поведение прототипа.

В частности, арифметика с плавающей точкой выполняется на Java double. Поэтому x<sup>y</sup> для 2 здесь равно 4,
а не 3.9999996 как в оригинале.

## Укороченный тест

См. Таблица I "Укороченный тест для контроля функционирования микрокалькулятора без периферийных устройств".

| Номер    | Ошибка                                      |
|----------|---------------------------------------------|
| 7        | Расхождение в точности вычислений           |
| 9        | Расхождение в точности вычислений           |
| 10       | Расхождение в точности вычислений           |
| 14       | Расхождение в точности вычислений           |
| 15       | Расхождение в точности вычислений           |
| 18       | Функциональность не реализована             |
| 21, 22   | ППЗУ не реализовано                         |   
| 23 - 149 | Необходимая функциональность не реализована |

## Полный тест

См. Таблица Ia "Тестовая последовательность для контроля функционирования микрокалькулятора без периферийных устройств".

| Номер    | Ошибка                                      |
|----------|---------------------------------------------|
| 6, 7     | ППЗУ не реализовано                         |
| 11       | Расхождение в точности вычислений           |
| 12,13    | Расхождение в точности вычислений           |
| 15,16    | Расхождение в точности вычислений           |
| 18 - 149 | Необходимая функциональность не реализована |