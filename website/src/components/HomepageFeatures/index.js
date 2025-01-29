const React = require('react');
import Container from './Container';
import GridBlock from './GridBlock';
import styles from './styles.css';

const Block = props => (
    <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
            align={props.align}
            contents={props.children}
            layout={props.layout}
        />
    </Container>
);

const easy = "**Easy to use**: quickly hop between your preflop charts while playing on desktop."
const games = "**Any game types**: switch profiles to use different range sets for different scenario, no matter the cash game or tournament format."
const random = "**No more indecision**: use the built-in randomiser to remove any culpability for your preflop actions."

const Feature1 = () => (
    <Block align="left">
        {[
            {
                content: [easy, games, random].join("\n\n"),
                image: `img/preview-chart.gif`,
                imageAlign: 'left',
                title: 'All Your Ranges in One Place'
            }
        ]}
    </Block>
);

const edit = "**Editing tools**: change an action's colour to fit your style, the percentages for individual hands, add custom raise sizes, and more!"
const impex = "**Import and Export**: share ranges with other users and try new ways to play by adding new profiles."
const free = "**Free FOREVER**: Preflop ranger is and always will be completely free, with no ads. This is a passion project!"

const Feature2 = () => (
    <Block align="left">
        {[
            {
                content: [edit, impex, free].join("\n\n"),
                image: `img/customise.png`,
                imageAlign: 'right',
                title: 'Customise and Share'
            }
        ]}
    </Block>
);

const Feature = feature => (
    <Block align="left">
        {[{
            content: feature.children,
            image: feature.image,
            imageAlign: feature.align,
            title: feature.title
        }]}
    </Block>
);

export default function HomepageFeatures() {
  return (
   <section className="mainContainer">
     <Feature1/>
     <Feature2/>
   </section>
  );
}
