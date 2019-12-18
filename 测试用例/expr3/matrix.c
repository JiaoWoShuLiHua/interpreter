// matrix multiplication
// basic test for no.2

int n = 3;
real a[n][n];
real b[n][n];

// initialize
for(int i = 0,int k = 1;i < n;i = i + 1)
  for(int j = 0;j < n;j =j + k){
    scan(a[i][j]);
  }


print("input b:\n");

for(int i = 0,int k = 1;i < n;i = i + 1)
  for(int j = 0;j < n;j =j + k){
    scan(b[i][j]);
  }

int k = -1;
real c[n][n];
for(int i = 0,int k = 1;i < n;i = i + 1)
  for(int j = 0;j < n;j =j + k){
    c[i][j] = 0;
    for(k = 0;k < n;k++){
      c[i][j] = c[i][j] + a[i][k] * b[k][j];
    }
}


// print c

for(int i = 0,int k = 1;i < n;i = i + 1){
  for(int j = 0;j < n;j =j + k){
    print(c[i][j]);
    //  print(' ');
  }
  //  print('\n');
 }
