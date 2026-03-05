package biseccion;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Biseccion extends JFrame {

    private JTextField txtFuncion, txtA, txtB, txtTol;
    private JLabel lblIter, lblRaiz, lblError;
    private DefaultTableModel modelo;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Biseccion().setVisible(true));
    }

    public Biseccion(){

        setTitle("Método de Bisección");
        setSize(850,550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Color fondo = new Color(245,248,252);

        JPanel principal = new JPanel(new BorderLayout(15,15));
        principal.setBorder(new EmptyBorder(20,20,20,20));
        principal.setBackground(fondo);
        setContentPane(principal);

        JLabel titulo = new JLabel("MÉTODO DE BISECCIÓN", JLabel.CENTER);
        titulo.setFont(new Font("Segoe UI",Font.BOLD,26));
        principal.add(titulo,BorderLayout.NORTH);

        JPanel entradas = new JPanel(new GridLayout(5,2,10,10));
        entradas.setBorder(new TitledBorder("Datos de entrada"));
        entradas.setBackground(fondo);

        txtFuncion = new JTextField();
        txtA = new JTextField();
        txtB = new JTextField();
        txtTol = new JTextField();

        entradas.add(new JLabel("Función f(x):"));
        entradas.add(txtFuncion);

        entradas.add(new JLabel("Límite inferior (a):"));
        entradas.add(txtA);

        entradas.add(new JLabel("Límite superior (b):"));
        entradas.add(txtB);

        entradas.add(new JLabel("Tolerancia:"));
        entradas.add(txtTol);

        JButton btnCalcular = new JButton("Calcular");
        JButton btnLimpiar = new JButton("Limpiar");

        entradas.add(btnCalcular);
        entradas.add(btnLimpiar);

        principal.add(entradas,BorderLayout.WEST);


        modelo = new DefaultTableModel(
                new String[]{"Iteración","xr","Error"},0);

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new TitledBorder("Iteraciones"));

        principal.add(scroll,BorderLayout.CENTER);

        JPanel resultados = new JPanel(new GridLayout(3,1));
        resultados.setBorder(new TitledBorder("Resultados"));
        resultados.setBackground(fondo);

        lblIter = new JLabel("Iteración donde paró:");
        lblRaiz = new JLabel("Valor final de x:");
        lblError = new JLabel("Error final:");

        resultados.add(lblIter);
        resultados.add(lblRaiz);
        resultados.add(lblError);

        principal.add(resultados,BorderLayout.SOUTH);

        btnCalcular.addActionListener(e->calcular());
        btnLimpiar.addActionListener(e->limpiar());
    }

    private void calcular(){

        try{

            modelo.setRowCount(0);

            String f = txtFuncion.getText().toLowerCase().replace(" ","");
            double a = Double.parseDouble(txtA.getText());
            double b = Double.parseDouble(txtB.getText());
            double tol = Double.parseDouble(txtTol.getText());

            double fa = evaluar(f,a);
            double fb = evaluar(f,b);

            if(fa*fb>0){
                JOptionPane.showMessageDialog(this,"No hay cambio de signo en el intervalo");
                return;
            }

            double xr=0;
            double xrAnt;
            double error=100;
            int i=0;

            while(error>tol){

                xrAnt=xr;
                xr=(a+b)/2;

                double fr=evaluar(f,xr);

                if(i>0)
                    error=Math.abs((xr-xrAnt)/xr);

                modelo.addRow(new Object[]{
                        i,
                        String.format("%.8f",xr),
                        i==0?"---":String.format("%.8f",error)
                });

                if(fa*fr<0){
                    b=xr;
                }else{
                    a=xr;
                    fa=fr;
                }

                i++;

                if(i>500) break;
            }

            lblIter.setText("Iteración donde paró: "+i);
            lblRaiz.setText("Valor final de x: "+String.format("%.10f",xr));
            lblError.setText("Error final: "+String.format("%.10f",error));

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Error en datos o función");
        }
    }

    private void limpiar(){
        txtFuncion.setText("");
        txtA.setText("");
        txtB.setText("");
        txtTol.setText("");
        modelo.setRowCount(0);
        lblIter.setText("Iteración donde paró:");
        lblRaiz.setText("Valor final de x:");
        lblError.setText("Error final:");
    }

    private double evaluar(final String str, final double xVal){
        return new Object(){
            int pos=-1,ch;
            void nextChar(){ch=(++pos<str.length())?str.charAt(pos):-1;}
            boolean eat(int charToEat){
                while(ch==' ')nextChar();
                if(ch==charToEat){nextChar();return true;}
                return false;
            }
            double parse(){nextChar();return parseExpression();}
            double parseExpression(){
                double x=parseTerm();
                for(;;){
                    if(eat('+'))x+=parseTerm();
                    else if(eat('-'))x-=parseTerm();
                    else return x;
                }
            }
            double parseTerm(){
                double x=parseFactor();
                for(;;){
                    if(eat('*'))x*=parseFactor();
                    else if(eat('/'))x/=parseFactor();
                    else return x;
                }
            }
            double parseFactor(){
                if(eat('+'))return parseFactor();
                if(eat('-'))return -parseFactor();
                double x;
                int startPos=this.pos;
                if(eat('(')){x=parseExpression();eat(')');}
                else if((ch>='0'&&ch<='9')||ch=='.'){
                    while((ch>='0'&&ch<='9')||ch=='.')nextChar();
                    x=Double.parseDouble(str.substring(startPos,this.pos));
                }
                else if(ch>='a'&&ch<='z'){
                    while(ch>='a'&&ch<='z')nextChar();
                    String name=str.substring(startPos,this.pos);
                    if(name.equals("x"))x=xVal;
                    else if(name.equals("e"))x=Math.E;
                    else{
                        x=parseFactor();
                        if(name.equals("sqrt"))x=Math.sqrt(x);
                        else if(name.equals("sin"))x=Math.sin(x);
                        else if(name.equals("cos"))x=Math.cos(x);
                        else if(name.equals("tan"))x=Math.tan(x);
                        else if(name.equals("ln"))x=Math.log(x);
                        else if(name.equals("log"))x=Math.log10(x);
                        else throw new RuntimeException();
                    }
                }
                else throw new RuntimeException();
                if(eat('^'))x=Math.pow(x,parseFactor());
                return x;
            }
        }.parse();
    }
}