import 'package:flutter/material.dart';
import 'package:momo_vn/momo_vn.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late MomoVn _momoPay;
  late PaymentResponse _momoPaymentResult;
  // ignore: non_constant_identifier_names
  late String _paymentStatus;
  @override
  void initState() {
    super.initState();
    _momoPay = MomoVn();
    _momoPay.on(MomoVn.EVENT_PAYMENT_SUCCESS, _handlePaymentSuccess);
    _momoPay.on(MomoVn.EVENT_PAYMENT_ERROR, _handlePaymentError);
    _paymentStatus = "";
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    if (!mounted) return;
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('THANH TOÁN QUA ỨNG DỤNG MOMO'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Column(
                children: [
                  MaterialButton(
                    color: Colors.blue,
                    textColor: Colors.white,
                    disabledColor: Colors.grey,
                    disabledTextColor: Colors.black,
                    padding: const EdgeInsets.all(8.0),
                    splashColor: Colors.blueAccent,
                    child: const Text('DEMO PAYMENT WITH MOMO.VN'),
                    onPressed: () async {
                      final MomoPaymentInfo options = MomoPaymentInfo(
                          merchantName: "SERO",
                          appScheme: 'momoq16m20230912',
                          merchantCode: 'MOMOQ16M20230912',
                          partnerCode: 'MOMOQ16M20230912',
                          amount: 60000,
                          orderId: '12321312',
                          orderLabel: 'Gói khám sức khoẻ',
                          merchantNameLabel: "HẸN KHÁM BỆNH",
                          fee: 10,
                          description: 'Thanh toán hẹn khám chữa bệnh',
                          username: '0352972441',
                          partner: 'merchant',
                          extra: "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                          isTestMode: true);
                      try {
                        _momoPay.open(options);
                      } catch (e) {
                        debugPrint(e.toString());
                      }
                    },
                  ),
                ],
              ),
              Text(_paymentStatus.isEmpty ? "CHƯA THANH TOÁN" : _paymentStatus)
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    _momoPay.clear();
  }

  void _setState() {
    _paymentStatus = 'Đã chuyển thanh toán';
    if (_momoPaymentResult.isSuccess == true) {
      _paymentStatus += "\nTình trạng: Thành công.";
      _paymentStatus += "\nSố điện thoại: ${_momoPaymentResult.phoneNumber}";
      _paymentStatus += "\nExtra: ${_momoPaymentResult.extra!}";
      _paymentStatus += "\nToken: ${_momoPaymentResult.token}";
    } else {
      _paymentStatus += "\nTình trạng: Thất bại.";
      _paymentStatus += "\nExtra: ${_momoPaymentResult.extra}";
      _paymentStatus += "\nMã lỗi: ${_momoPaymentResult.status}";
    }
  }

  void _handlePaymentSuccess(PaymentResponse response) {
    setState(() {
      _momoPaymentResult = response;
      _setState();
    });
  }

  void _handlePaymentError(PaymentResponse response) {
    setState(() {
      _momoPaymentResult = response;
      _setState();
    });
  }
}
