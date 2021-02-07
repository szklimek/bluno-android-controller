#include <LiquidCrystal.h>

LiquidCrystal lcd(
  12 /* RS pin */,
  11 /* E (enable) */,
  5 /* D4 */,
  4 /* D5 */,
  3/* D6 */,
  2/* D7 */);

const int contrastPin = 6;

void setup() {
  // Initial the Serial for BLE connection
  Serial.begin(115200);

  lcd.begin(16 /* Columns */, 2 /* Rows */);
  pinMode(contrastPin, OUTPUT);

  // Initialize proper lcd contrast
  analogWrite(contrastPin, 0);
}

void loop() {
  if (Serial.available())  {
    String receivedString = Serial.readString();

    // Serial.write expects C-type string
    char* convertedString = (char*) malloc(sizeof(char)*(receivedString.length() + 1));
    receivedString.toCharArray(convertedString, receivedString.length() + 1);

    Serial.write(convertedString);
    Serial.println();
    lcd.clear();
    lcd.print(receivedString);
  }

  delay(50);
}
