int main() {
int n;
n = scan();
real err = 1010101010101010.101010101010;
real clc = 1000000000000000.000000000001;
int a[n];
int head = 0,tail = 0;

int op;
real operand;
operand= scan(); // abs(0): exit >1:push <-1:pop -1~1:print len;
op = scan();

while(operand <> 0){
  if(operand > 1){
    // push
    int isFull = (tail + 1) - (tail + 1) / n * n  == head;
    if(isFull){
      print(err);
      break;
    }
    a[tail] = op;
    tail = (tail + 1) - ((tail + 1) / n) * n;
  }else if(operand < -1){
    // pop
    int isEmpty = tail == head;
    if(isEmpty){
      print(-err);
      break;
    }
    print(a[head]);
    head = (head + 1) - ((head + 1) / n ) * n;
  }else {
    // print length
    int len = tail - head - (tail - head) / n * n;
    print(len);
  }
  print(clc);
  operand = scan();
  op = scan();

 }
}

