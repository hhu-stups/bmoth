MACHINE send_more_money_no_inv_violation
CONSTANTS S,E,N,D, M,O,R, Y
PROPERTIES
   S : 1..9 & E : 0..9 & N : 0..9 &
   D : 0..9 & M : 1..9 & O : 0..9 & 
   R : 0..9 & Y : 0..9 &
   S >0 & M >0 & 
    S /= E & S /= N & S /= D & S /= M & S /= O & S /= R & S/= Y &
    E /= N & E /= D & E /= M & E /= O & E /= R & E/= Y &
    N /= D & N /= M & N /= O & N /= R & N/= Y &
    D /= M & D /= O & D /= R & D/= Y &
    M /= O & M /= R & M/= Y&
    O /= R & O/= Y &
    R /= Y
  &
   S*1000 + E*100 + N*10 + D +
   M*1000 + O*100 + R*10 + E =
   M*10000 + O*1000 + N*100 + E*10 + Y
VARIABLES sum
INVARIANT sum : INTEGER & 
    R=8 & S=9 & D=7 & E=5 & Y=2 & M=1 & N=6 & O=0 & sum=10652
INITIALISATION sum := M*10000 + O*1000 + N*100 + E*10 + Y
END
