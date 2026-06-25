import 'package:flutter_test/flutter_test.dart';

import 'package:incluapp/main.dart';

void main() {
  testWidgets('LexiEdu muestra la pantalla splash', (tester) async {
    await tester.pumpWidget(const LexiEduApp());
    await tester.pump();

    expect(find.text('LexiEdu'), findsWidgets);
  });
}
