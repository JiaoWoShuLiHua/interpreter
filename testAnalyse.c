int main() {
int len = 11;
real a[len] = { -100.1,1,2,3,4,5.5,6,7,8,9,100.9};
real b  = 0;
real err = 010101010110101.1010101011010101;
b = scan();

int l = 0, h = len;
while(l < h ){
  int mid = l + ( h - l)/ 2;
  if(a[mid] > b){
    h = mid;
  }else if(a[mid] < b)
    l = mid + 1;
  else {
    break;
  }
 }

if(l < h - 1)
  {
  print(l);
  print(h);
  print(l + ( h - l) / 2);
  }
 else{
  print("err");
  print(err);
 }
}
