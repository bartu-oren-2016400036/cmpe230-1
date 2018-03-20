jmp start

num: dw 0def2h,0ffffh,0abc1h,0ffffh
sum: dw 00000h,00000h
digit: dw 00000h,00000h,00000h,00000h

start:
	mov ax, [num+2]
	add ax, [num+6]
	mov dx, [num]
	adc dx, [num+4]
	
	mov [sum], dx
	mov [sum+2], ax
	call print

	
	mov ax, [sum+2]
	mov [sum], ax
	call print
	
	mov ah, 4ch 
	int 21h
	
print:
	mov ax, 0
	mov ax, [sum]
	shr ax, 12
	mov [digit],ax

	mov ax, [sum]
	shl ax, 4
	shr ax, 12
	mov [digit+2], ax
	
	mov ax, [sum]
	shl ax, 8
	shr ax, 12
	mov [digit+4], ax
	
	mov ax, [sum]
	shl ax, 12
	shr ax, 12
	mov [digit+6], ax
	
	mov ax, 0
	
	mov ah,02
	mov dx,[digit]
	add dx, 30h
	cmp dx, 57
	jle less
	add dx, 27h
	less:
	int 21h
	
	mov dx,[digit+2]
	add dx, 30h
	cmp dx, 57
	jle less2
	add dx, 27h
	less2:
	int 21h
	
	mov dx,[digit+4]
	add dx, 30h
	cmp dx, 57
	jle less3
	add dx, 27h
	less3:
	int 21h
	
	mov dx,[digit+6]
	add dx, 30h
	cmp dx, 57
	jle less4
	add dx, 27h
	less4:
	int 21h
	ret