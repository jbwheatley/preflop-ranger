import React from 'react';
import ReactMarkdown from 'react-markdown';
import Link from '@docusaurus/Link';
import Translate, {translate} from '@docusaurus/Translate';

const renderers = {
  link: ({node: _node, ...props}) => <Link {...props} />,
};

function getMarkdown(children) {
  if (children?.type === Translate) {
    return translate({
      id: children.props.id,
      message: children?.props?.children,
    });
  } else {
    return children;
  }
}

export default function MarkdownBlock({children}) {
  const markdown = getMarkdown(children);
  return (
    <div>
      <span>
        <ReactMarkdown renderers={renderers}>{markdown}</ReactMarkdown>
      </span>
    </div>
  );
}